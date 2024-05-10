package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Order
import com.mixfa.marketplace.marketplace.model.PRODUCT_MONGO_COLLECTION
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.model.RealizedProduct
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import com.mixfa.marketplace.marketplace.model.discount.DiscountByCategory
import com.mixfa.marketplace.marketplace.model.discount.DiscountByProduct
import com.mixfa.marketplace.marketplace.service.repo.ProductRepository
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.ProductCharacteristicsNotSetException
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.model.MarketplaceEvent
import com.mixfa.marketplace.shared.model.QueryConstructor
import com.mixfa.marketplace.shared.model.SortConstructor
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.productNotFound
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.*

@Service
@Validated
class ProductService(
    private val productRepo: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val categoryService: CategoryService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<MarketplaceEvent> {
    fun findProductById(id: String): Optional<Product> = productRepo.findById(id)

    fun findProductsByIdsOrThrow(ids: Collection<String>): List<Product> {
        val products = productRepo.findAllById(ids)
        if (products.size != ids.size) throw NotFoundException.productNotFound()
        return products
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:EDIT')")
    private fun updateProductRate(product: Product, newRate: Double): Product {
        var newProductRate = (product.rate + newRate) / (if (product.rate == 0.0) 1.0 else 2.0)
        if (newProductRate < 0.0) newProductRate = 0.0
        return productRepo.save(product.copy(rate = newProductRate))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addProductImage(productId: String, @NotBlank imageLink: String): Product {
        val product = findProductById(productId).orThrow()

        if (!product.images.contains(imageLink))
            return productRepo.save(product.copy(images = product.images + imageLink))

        return product
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun removeProductImage(productId: String, imageLink: String): Product {
        val product = findProductById(productId).orThrow()

        if (product.images.contains(imageLink))
            return productRepo.save(product.copy(images = product.images - imageLink))

        return product
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerProduct(@Valid request: Product.RegisterRequest): Product {
        val categories = categoryService.findCategoriesByIdOrThrow(request.categories)

        val productCharacteristicsKeys = request.characteristics.keys

        for (category in categories)
            if (!productCharacteristicsKeys.containsAll(category.requiredProps))
                throw ProductCharacteristicsNotSetException(
                    category.requiredProps, productCharacteristicsKeys
                )

        return productRepo.save(
            Product(
                caption = request.caption,
                categories = categories.toHashSet(),
                characteristics = request.characteristics,
                description = request.description,
                price = request.price,
                availableQuantity = request.availableQuantity,
                images = request.images
            )
        ).also { product -> eventPublisher.publishEvent(Event.ProductRegister(product, this)) }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteProduct(productId: String) {
        val product = productRepo.findById(productId).orThrow()

        productRepo.delete(product)
        eventPublisher.publishEvent(Event.ProductDelete(product, this))
    }

    fun findProducts(query: String, pageable: CheckedPageable): Page<Product> {
        return productRepo.findAllByCaptionContainingIgnoreCase(query, pageable)
    }

    fun findProducts(
        queryConstructor: QueryConstructor,
        sortConstructor: SortConstructor,
        pageable: CheckedPageable
    ): List<Product> {
        val query = queryConstructor.makeQuery()
        val sort = sortConstructor.makeSort()

        query.with(pageable).with(sort)

        return mongoTemplate.find(query, Product::class.java, PRODUCT_MONGO_COLLECTION)
    }

    fun countProducts() = productRepo.count()

    private fun handleOrderRegistration(order: Order) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(order.products.map(RealizedProduct::productId))),
            Update().inc(Product::ordersCount.name, 1),
            Product::class.java,
            PRODUCT_MONGO_COLLECTION
        )

        for (product in order.products) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(product.productId)),
                Update().inc(Product::availableQuantity.name, -product.quantity),
                Product::class.java,
                PRODUCT_MONGO_COLLECTION
            )
        }
    }

    private fun handleOrderCancellation(order: Order) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(order.products.map(RealizedProduct::productId))),
            Update().inc(Product::ordersCount.name, -1),
            Product::class.java,
            PRODUCT_MONGO_COLLECTION
        )

        for (product in order.products) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(product.productId)),
                Update().inc(Product::availableQuantity.name, product.quantity),
                Product::class.java,
                PRODUCT_MONGO_COLLECTION
            )
        }
    }

    private fun updateProductsPrices(discount: AbstractDiscount, discountDeleted: Boolean) {
        val targetProducts = when (discount) {
            is DiscountByCategory -> {
                val categoriesSet = discount.buildCategoriesSet()
                productRepo.findAllByCategoriesContains(categoriesSet) // add #iteratepages
            }

            is DiscountByProduct -> discount.targetProducts
            else -> emptyList()
        }

        for (product in targetProducts)
            productRepo.save(
                product.copy(
                    actualPrice = if (discountDeleted) product.actualPrice / discount.multiplier else product.actualPrice * discount.multiplier
                )
            )
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is CommentService.Event.CommentRegister -> updateProductRate(event.comment.product, event.comment.rate)
            is CommentService.Event.CommentDelete -> updateProductRate(event.comment.product, -event.comment.rate)
            is OrderService.Event.OrderRegister -> handleOrderRegistration(event.order)
            is OrderService.Event.OrderCancel -> handleOrderCancellation(event.order)

            is DiscountService.Event.DiscountRegister -> updateProductsPrices(event.discount, false)
            is DiscountService.Event.DiscountDelete -> updateProductsPrices(event.discount, true)
        }
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class ProductRegister(val product: Product, src: Any) : Event(src)
        class ProductDelete(val product: Product, src: Any) : Event(src)
    }
}

