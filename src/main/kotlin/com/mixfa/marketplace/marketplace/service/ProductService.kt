package com.mixfa.marketplace.marketplace.service

import com.mixfa.excify.FastThrowable
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.model.RealizedProduct
import com.mixfa.marketplace.marketplace.service.repo.ProductRepository
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.ProductCharacteristicsNotSetException
import com.mixfa.marketplace.shared.event.MarketplaceEvent
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.model.PrecompiledSort
import com.mixfa.marketplace.shared.model.QueryConstructor
import com.mixfa.marketplace.shared.model.SortConstructor
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.productNotFound
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
import java.util.*

@Service
class ProductService(
    private val productRepo: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val categoryService: CategoryService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<MarketplaceEvent> {
    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun findProductById(id: String): Optional<Product> = productRepo.findById(id)

    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun findProductsByIdsOrThrow(ids: List<String>): List<Product> {
        val products = productRepo.findAllById(ids)
        if (products.size != ids.size) throw NotFoundException.productNotFound()
        return products
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:WRITE')")
    fun updateProductRate(product: Product, newRate: Double): Product {
        var newProductRate = (product.rate + newRate) / (if (product.rate == 0.0) 1.0 else 2.0)
        if (newProductRate < 0.0) newProductRate = 0.0
        return productRepo.save(product.copy(rate = newProductRate))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addProductImage(productId: String, imageLink: String): Product {
        if (imageLink.isBlank())
            throw FastThrowable("Can`t add $imageLink to product images")

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
    fun registerProduct(request: Product.RegisterRequest): Product {
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
                characteristics = request.characteristics,
                description = request.description,
                price = request.price,
                categories = categories,
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

    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun findProducts(query: String, pageable: CheckedPageable): Page<Product> {
        return productRepo.findAllByText(query, pageable)
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun findProducts(
        queryConstructor: QueryConstructor,
        sortConstructor: SortConstructor,
        pageable: CheckedPageable
    ): List<Product> {
        val query = queryConstructor.makeQuery()
        val sort = sortConstructor.makeSort()

        query.with(pageable).with(sort)

        return mongoTemplate.find(query, Product::class.java)
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun findProducts(
        queryConstructor: QueryConstructor,
        precompiledSort: PrecompiledSort,
        pageable: CheckedPageable
    ): List<Product> {
        val query = queryConstructor.makeQuery()
        query.with(pageable).with(precompiledSort.sort)

        return mongoTemplate.find(query, Product::class.java)
    }

    @PreAuthorize("hasAuthority('MARKETPLACE:READ')")
    fun countProducts() = productRepo.count()

    private fun incProductsOrdersCount(productsIds: List<ObjectId>) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(productsIds)),
            Update().inc("ordersCount", 1),
            Product::class.java
        )
    }

    private fun decProductOrdersCount(productsIds: List<ObjectId>) {
        mongoTemplate.updateMulti(
            Query(Criteria.where("_id").`in`(productsIds)),
            Update().inc("ordersCount", -1),
            Product::class.java
        )
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is CommentService.Event.CommentRegister -> updateProductRate(event.comment.product, event.comment.rate)
            is CommentService.Event.CommentDelete -> updateProductRate(event.comment.product, -event.comment.rate)
            is OrderService.Event.OrderRegister -> incProductsOrdersCount(event.order.products.map(RealizedProduct::productId))
            is OrderService.Event.OrderCancel -> decProductOrdersCount(event.order.products.map(RealizedProduct::productId))
        }
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class ProductRegister(val product: Product, src: Any) : Event(src)
        class ProductDelete(val product: Product, src: Any) : Event(src)
    }
}

