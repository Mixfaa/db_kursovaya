package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import com.mixfa.marketplace.marketplace.model.discount.DiscountByCategory
import com.mixfa.marketplace.marketplace.model.discount.DiscountByProduct
import com.mixfa.marketplace.marketplace.service.repo.ProductRepository
import com.mixfa.shared.*
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import com.mixfa.shared.model.QueryConstructor
import com.mixfa.shared.model.SortConstructor
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Validated
class ProductService(
    private val productRepo: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val categoryService: CategoryService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<MarketplaceEvent> {
    fun findProductById(id: String): Optional<Product> = productRepo.findById(id)
    fun productExists(id: String): Boolean = productRepo.existsById(id)

    fun findProductsByIdsOrThrow(ids: Collection<String>): List<Product> {
        val products = productRepo.findAllById(ids)
        if (products.size != ids.size) throw NotFoundException.productNotFound()
        return products
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:EDIT')")
    private fun updateProductRate(product: Product, newRate: Double) {
        if (newRate < 0.0) return

        val newProductRate = if (product.rate == 0.0) newRate else doubleArrayOf(product.rate, newRate).average()

        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(product.id)),
            Update.update(fieldName(Product::rate), newProductRate),
            PRODUCT_MONGO_COLLECTION
        )
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addProductImage(productId: String, @NotBlank imageLink: String) {
        if (!productExists(productId)) throw NotFoundException.productNotFound()

        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(productId)),
            Update().addToSet(fieldName(Product::images), imageLink),
            PRODUCT_MONGO_COLLECTION
        )
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun removeProductImage(productId: String, imageLink: String) {
        if (!productExists(productId)) throw NotFoundException.productNotFound()

        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(productId)),
            Update().pull(fieldName(Product::images), imageLink),
            PRODUCT_MONGO_COLLECTION
        )
    }

    private fun buildAllRelatedCategoriesIdsList(rootCategories: Set<Category>): List<String> {
        fun addCategory(list: MutableList<String>, id: String) {
            val category = categoryService.findCategoryById(id).getOrNull() ?: return
            list.add(category.id.toString())

            category.parentCategoryId?.let { parentId -> addCategory(list, parentId) }
        }

        return buildList {
            rootCategories.forEach { add(it.id.toString()) }
            rootCategories.forEach {
                it.parentCategoryId?.let { id -> addCategory(this, id) }
            }
        }
    }

    private fun checkProductCharacteristics(
        characteristics: Map<String, String>,
        categories: Collection<Category>
    ) {
        val productCharacteristicsKeys = characteristics.keys

        for (category in categories)
            if (!productCharacteristicsKeys.containsAll(category.requiredProps))
                throw ProductCharacteristicsNotSetException(
                    category.requiredProps, productCharacteristicsKeys
                )
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun updateProduct(productId: String, request: Product.RegisterRequest): Product {
        if (!productExists(productId)) throw NotFoundException.productNotFound()

        val categories = categoryService.findCategoriesByIdOrThrow(request.categories).toHashSet()

        checkProductCharacteristics(request.characteristics, categories)

        return productRepo.save(
            Product(
                id = ObjectId(productId),
                caption = request.caption,
                categories = categories,
                allRelatedCategoriesIds = buildAllRelatedCategoriesIdsList(categories),
                characteristics = request.characteristics,
                description = request.description,
                price = request.price,
                availableQuantity = request.availableQuantity,
                images = request.images
            )
        )
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    open fun registerProduct(@Valid request: Product.RegisterRequest): Product {
        val categories = categoryService.findCategoriesByIdOrThrow(request.categories).toHashSet()
        checkProductCharacteristics(request.characteristics, categories)

        return productRepo.save(
            Product(
                caption = request.caption,
                categories = categories,
                allRelatedCategoriesIds = buildAllRelatedCategoriesIdsList(categories),
                characteristics = request.characteristics,
                description = request.description,
                price = request.price,
                availableQuantity = request.availableQuantity,
                images = request.images
            )
        ).also { product -> eventPublisher.publishEvent(Event.ProductRegister(product, this)) }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    open fun deleteProduct(productId: String) {
        if (!productRepo.existsById(productId)) throw NotFoundException.productNotFound()

        eventPublisher.publishEvent(Event.ProductDelete(productId, this))
        productRepo.deleteById(productId)
    }

    fun findProducts(query: String, pageable: CheckedPageable): Page<Product> {
        return productRepo.findAllByCaptionContainingIgnoreCase(query, pageable)
    }

    fun findProducts(
        queryConstructor: QueryConstructor,
        sortConstructor: SortConstructor,
        pageable: CheckedPageable
    ): Page<Product> {
        val query = queryConstructor.makeQuery()
        val sort = sortConstructor.makeSort()

        query.with(pageable).with(sort)

        val total = mongoTemplate.count(query, PRODUCT_MONGO_COLLECTION)
        val products = mongoTemplate.find(query, Product::class.java, PRODUCT_MONGO_COLLECTION)

        return PageImpl(products, pageable, total)
    }

    fun countProducts() = productRepo.count()

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    fun changeProductQuantity(productId: String, quantity: Long) {
        val updateResult = mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(productId)),
            Update.update(fieldName(Product::availableQuantity), quantity),
            PRODUCT_MONGO_COLLECTION
        )

        if (updateResult.nothingModified()) throw NotFoundException.productNotFound()
    }

    private fun handleOrderRegistration(order: Order) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(order.products.map(RealizedProduct::productId))),
            Update().inc(fieldName(Product::ordersCount), 1),
            PRODUCT_MONGO_COLLECTION
        )

        for (product in order.products) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(product.productId)),
                Update().inc(fieldName(Product::availableQuantity), -product.quantity),
                PRODUCT_MONGO_COLLECTION
            )
        }
    }

    private fun handleOrderCancellation(order: Order) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(order.products.map(RealizedProduct::productId))),
            Update().inc(fieldName(Product::ordersCount), -1),
            PRODUCT_MONGO_COLLECTION
        )

        for (product in order.products) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(product.productId)),
                Update().inc(fieldName(Product::availableQuantity), product.quantity),
                PRODUCT_MONGO_COLLECTION
            )
        }
    }

    private fun updateProductsPrices(discount: AbstractDiscount, discountDeleted: Boolean) {
        val targetProducts = when (discount) {
            is DiscountByCategory -> {
                mongoTemplate.findIterating<Product>(
                    Query(Criteria.where(fieldName(Product::allRelatedCategoriesIds)).`in`(discount.allCategoriesIds)),
                    PRODUCT_MONGO_COLLECTION
                )
            }

            is DiscountByProduct -> discount.targetProducts
            else -> emptyList()
        }

        for (product in targetProducts) {
            mongoTemplate.updateFirst(
                Query(Criteria.where("_id").`is`(product.id)),
                Update.update(
                    Product::actualPrice.name,
                    if (discountDeleted) product.actualPrice / discount.multiplier else product.actualPrice * discount.multiplier
                ),
                PRODUCT_MONGO_COLLECTION
            )
        }
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
        class ProductDelete(
            val productId: String,
            src: Any,
            var product: Product? = null,
        ) : Event(src)
    }
}

