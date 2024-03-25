package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.ProductRepository
import com.mixfa.marketplace.shared.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepo: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val categoryService: CategoryService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<CommentService.Event> {
    fun findProductById(id: String) = productRepo.findById(id)

    fun findProductsByIdsOrThrow(ids: List<String>): List<Product> {
        val products = productRepo.findAllById(ids)
        if (products.isEmpty()) throw NotFoundException.productNotFound()
        return products
    }

    fun updateProductRate(product: Product, newRate: Double): Product {
        var newProductRate = (product.rate + newRate) / (if (product.rate == 0.0) 1.0 else 2.0)
        if (newProductRate < 0.0) newProductRate = 0.0
        return productRepo.save(product.copy(rate = newProductRate))
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerProduct(request: Product.CreateRequest): Product {
        val categories = categoryService.findCategoriesByIdOrThrow(request.categories)

        val productCharacteristicsKeys = request.characteristics.keys

        for (category in categories)
            if (!productCharacteristicsKeys.containsAll(category.requiredProps))
                throw ProductCharacteristicsNotSetException.make(
                    category.requiredProps, productCharacteristicsKeys
                )

        return productRepo.save(
            Product(
                caption = request.caption,
                characteristics = request.characteristics,
                description = request.description,
                price = request.price,
                categories = categories
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
        return productRepo.findAllByText(query, pageable)
    }

    fun findProducts(
        queryConstructor: QueryConstructor,
        sortConstructor: SortConstructor,
        pageable: CheckedPageable
    ): List<Product> {
        val query = queryConstructor.makeQuery()
        val sort = sortConstructor.makeSort()

        query.with(pageable).with(sort) // applies to same instance

        return mongoTemplate.find(query, Product::class.java)
    }

    fun countProducts() = productRepo.count()

    override fun onApplicationEvent(event: CommentService.Event) {
        when (event) {
            is CommentService.Event.CommentRegister -> updateProductRate(event.comment.product, event.comment.rate)
            is CommentService.Event.CommentDelete -> updateProductRate(event.comment.product, -event.comment.rate)
        }
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class ProductRegister(val product: Product, src: Any) : Event(src)
        class ProductDelete(val product: Product, src: Any) : Event(src)
    }
}

