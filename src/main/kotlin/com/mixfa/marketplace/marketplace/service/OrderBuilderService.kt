package com.mixfa.marketplace.marketplace.service

import com.mixfa.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.ORDER_BUILDER_COLLECTION
import com.mixfa.marketplace.marketplace.model.Order
import com.mixfa.marketplace.marketplace.model.OrderBuilder
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.OrderBuilderRepo
import com.mixfa.shared.*
import com.mixfa.shared.model.MarketplaceEvent
import org.springframework.context.ApplicationListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Validated
@Service
class OrderBuilderService(
    private val orderBuilderRepo: OrderBuilderRepo,
    private val accountService: AccountService,
    private val productService: ProductService,
    private val orderService: OrderService,
    private val mongoTemplate: MongoTemplate
) : ApplicationListener<MarketplaceEvent> {
    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun getOrderBuilder(): OrderBuilder {
        val orderBuilder = orderBuilderRepo.findByOwnerUsername(authenticatedPrincipal().name)

        return orderBuilder ?: orderBuilderRepo.save(
            OrderBuilder(
                owner = accountService.getAuthenticatedAccount().orThrow(),
                productsIds = emptyMap()
            )
        )
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun addProduct(productId: String, quantity: Long) {
        val orderBuilder = getOrderBuilder()
        if (!productService.productExists(productId)) throw NotFoundException.productNotFound()

        orderBuilderRepo.save(
            orderBuilder.copy(
                productsIds = orderBuilder.productsIds + (productId to quantity)
            )
        )
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun removeProduct(productId: String) {
        val orderBuilder = getOrderBuilder()
        if (!productService.productExists(productId)) throw NotFoundException.productNotFound()

        orderBuilderRepo.save(
            orderBuilder.copy(
                productsIds = orderBuilder.productsIds - productId
            )
        )
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun makeOrder(shippingAddress: String, promoCode: String?): Order {
        val orderBuilder = getOrderBuilder()
        val products = buildMap {
            for ((id, quantity) in orderBuilder.productsIds) {
                val product = productService.findProductById(id).orThrow()
                put(product, quantity)
            }
        }

        return orderService.registerOrder(
            OrderBuilder.WithOrderData(
                orderBuilder,
                products,
                shippingAddress,
                promoCode
            )
        )
            .also {
                orderBuilderRepo.delete(orderBuilder)
            }
    }

    private fun handleProductDeletion(product: Product) {
        val stringId = product.id.toString()
        mongoTemplate.findIterating<OrderBuilder>(
            Query(Criteria.where("productsIds.$stringId").exists(true)),
            ORDER_BUILDER_COLLECTION
        ) { orderBuilders ->
            for (orderBuilder in orderBuilders)
                orderBuilderRepo.save(orderBuilder.copy(productsIds = orderBuilder.productsIds - stringId))
        }
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is ProductService.Event.ProductDelete -> handleProductDeletion(event.product)
        }
    }
}