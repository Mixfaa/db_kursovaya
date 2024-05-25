package com.mixfa.marketplace.marketplace.service

import com.mixfa.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.Order
import com.mixfa.marketplace.marketplace.model.OrderBuilder
import com.mixfa.marketplace.marketplace.service.repo.OrderBuilderRepo
import com.mixfa.shared.IS_AUTHENTICATED
import com.mixfa.shared.authenticatedPrincipal
import com.mixfa.shared.orThrow
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Validated
@Service
class OrderBuilderService(
    private val orderBuilderRepo: OrderBuilderRepo,
    private val accountService: AccountService,
    private val productService: ProductService,
    private val orderService: OrderService
) {
    @PreAuthorize(IS_AUTHENTICATED)
    fun getOrderBuilder(): OrderBuilder {
        var orderBuilder = orderBuilderRepo.findByOwnerUsername(authenticatedPrincipal().name)
        if (orderBuilder == null) {
            orderBuilder = orderBuilderRepo.save(
                OrderBuilder(
                    owner = accountService.getAuthenticatedAccount().orThrow(),
                    products = emptyMap()
                )
            )
        }

        return orderBuilder!!
    }

    @PreAuthorize(IS_AUTHENTICATED)
    fun addProduct(productId: String, quantity: Long) {
        val orderBuilder = getOrderBuilder()
        val product = productService.findProductById(productId).orThrow()

        orderBuilderRepo.save(
            orderBuilder.copy(
                products = orderBuilder.products + (product to quantity)
            )
        )
    }

    @PreAuthorize(IS_AUTHENTICATED)
    fun removeProduct(productId: String, quantity: Long) {
        val orderBuilder = getOrderBuilder()
        val product = productService.findProductById(productId).orThrow()

        orderBuilderRepo.save(
            orderBuilder.copy(
                products = orderBuilder.products - product
            )
        )
    }

    @PreAuthorize(IS_AUTHENTICATED)
    fun makeOrder(shippingAddress: String, promoCode: String?): Order {
        val orderBuilder = getOrderBuilder()
        return orderService.registerOrder(OrderBuilder.WithOrderData(orderBuilder, shippingAddress, promoCode))
            .also {
                orderBuilderRepo.delete(orderBuilder)
            }
    }
}