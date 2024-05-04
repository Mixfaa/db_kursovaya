package com.mixfa.marketplace.marketplace.service

import com.mixfa.excify.FastException
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.service.repo.OrderRepository
import com.mixfa.marketplace.shared.authenticatedPrincipal
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.model.MarketplaceEvent
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.throwIfNot
import jakarta.validation.Valid
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class OrderService(
    private val orderRepo: OrderRepository,
    private val accountService: AccountService,
    private val discountService: DiscountService,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun calculateOrderCost(@Valid request: Order.RegisterRequest): TempOrder {
        val products = productService.findProductsByIdsOrThrow(request.products)
        return TempOrder(processDiscounts(products, request.promoCode))
    }

    private fun processDiscounts(products: List<Product>, promoCode: String?): List<RealizedProduct> {
        val realizedProductBuilders = products.asSequence().map { RealizedProduct.Builder(it) }

        val promoCodeDiscount = promoCode?.let { code -> discountService.findPromoCode(code) }
        if (promoCodeDiscount != null)
            realizedProductBuilders.forEach { it.applyDiscount(promoCodeDiscount) }

        return realizedProductBuilders.map { it.build() }.toList()
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun registerOrder(@Valid request: Order.RegisterRequest): Order {
        val account = accountService.getAuthenticatedAccount().orThrow()
        val products = productService.findProductsByIdsOrThrow(request.products)

        if (products.size != request.products.size) throw FastException("Can`t load all requested products")

        val realizedProducts = processDiscounts(products, request.promoCode)

        if (realizedProducts.size != request.products.size) throw FastException("Can`t process all requested products")

        return orderRepo.save(
            Order(
                products = realizedProducts,
                owner = account,
                status = OrderStatus.UNPAID,
                shippingAddress = request.shippingAddress
            )
        ).also { order ->
            eventPublisher.publishEvent(Event.OrderRegister(order, this))
        }
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun listMyOrders(pageable: CheckedPageable): Page<Order> {
        val principal = authenticatedPrincipal()
        return orderRepo.findAllByOwnerUsername(principal.name, pageable)
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun countMyOrders(): Long {
        val principal = authenticatedPrincipal()
        return orderRepo.countByOwnerUsername(principal.name)
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun cancelOrder(orderId: String): Order {
        val order = orderRepo.findById(orderId).orThrow()
        authenticatedPrincipal().throwIfNot(order.owner)

        var canceledOrder = order.copy(status = OrderStatus.CANCELED)
        canceledOrder = orderRepo.save(canceledOrder)

        eventPublisher.publishEvent(Event.OrderCancel(order, this))
        return canceledOrder
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun changeOrderStatus(orderId: String, newStatus: OrderStatus): Order {
        val order = orderRepo.findById(orderId).orThrow()
        return orderRepo.save(order.copy(status = newStatus))
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class OrderRegister(val order: Order, src: Any) : Event(src)
        class OrderCancel(val order: Order, src: Any) : Event(src)
    }
}

