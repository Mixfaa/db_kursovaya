package com.mixfa.marketplace.marketplace.service

import com.mixfa.account.service.AccountService
import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.marketplace.marketplace.model.ORDER_BUILDER_COLLECTION
import com.mixfa.marketplace.marketplace.model.Order
import com.mixfa.marketplace.marketplace.model.OrderBuilder
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.OrderBuilderRepo
import com.mixfa.shared.NotFoundException
import com.mixfa.shared.authenticatedPrincipal
import com.mixfa.shared.model.MarketplaceEvent
import com.mixfa.shared.orThrow
import com.mixfa.shared.productNotFound
import org.springframework.context.ApplicationListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
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
    private fun findOrderBuilder(): OrderBuilder? = orderBuilderRepo.findByOwnerUsername(authenticatedPrincipal().name)

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun getOrderBuilder(): OrderBuilder {
        return findOrderBuilder() ?: orderBuilderRepo.save(
            OrderBuilder(
                owner = accountService.getAuthenticatedAccount().orThrow(),
                productsIds = emptyMap()
            )
        )
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun addProduct(productId: String, quantity: Long) {
        if (!productService.productExists(productId)) throw NotFoundException.productNotFound()

        val principalName = authenticatedPrincipal().name
        if (orderBuilderRepo.existsByOwnerUsername(principalName)) {
            val orderBuilder = findOrderBuilder()!!
            orderBuilderRepo.save(
                orderBuilder.copy(
                    productsIds = orderBuilder.productsIds + (productId to quantity)
                )
            )
        } else {
            orderBuilderRepo.save(
                OrderBuilder(
                    owner = accountService.getAuthenticatedAccount().orThrow(),
                    productsIds = mapOf(productId to quantity)
                )
            )
        }
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun removeProduct(productId: String) {
        val orderBuilder = findOrderBuilder() ?: return

        orderBuilderRepo.save(
            orderBuilder.copy(
                productsIds = orderBuilder.productsIds - productId // it's a map, so i can't use mongoTemplate
            )
        )
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun makeOrder(shippingAddress: String, promoCode: String?): Order {
        if (shippingAddress.isBlank()) throw makeMemorizedException("Shipping address can`t be empty")
        val orderBuilder = findOrderBuilder() ?: throw makeMemorizedException("You don't have any orders")
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
        mongoTemplate.updateMulti(
            Query(Criteria.where("productsIds.$stringId").exists(true)),
            Update().pull("productsIds", stringId),
            ORDER_BUILDER_COLLECTION
        )

//
//        mongoTemplate.findIterating<OrderBuilder>(
//            Query(Criteria.where("productsIds.$stringId").exists(true)),
//            ORDER_BUILDER_COLLECTION
//        ) { orderBuilders ->
//            for (orderBuilder in orderBuilders)
//                orderBuilderRepo.save(orderBuilder.copy(productsIds = orderBuilder.productsIds - stringId))
//        }
    }

    override fun onApplicationEvent(event: MarketplaceEvent) {
        when (event) {
            is ProductService.Event.ProductDelete -> handleProductDeletion(event.product)
        }
    }
}