package com.mixfa.marketplace.marketplace.service

import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.excify.FastException
import com.mixfa.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.service.repo.OrderRepository
import com.mixfa.shared.authenticatedPrincipal
import com.mixfa.shared.contains
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import com.mixfa.shared.orThrow
import com.mixfa.shared.throwIfNot
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
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun calculateOrderCost(@Valid request: Order.RegisterRequest): TempOrder {
        val products = productService
            .findProductsByIdsOrThrow(request.products.keys)
            .associateWith(request::findProductQuantity)
        return TempOrder(processDiscounts(products, request.promoCode))
    }

    private fun processDiscounts(products: Map<Product, Long>, promoCode: String?): List<RealizedProduct> {
        val realizedProductBuilders =
            products.asSequence().map { (product, quantity) -> RealizedProduct.Builder(product, quantity) }

        val promoCodeDiscount = promoCode?.let { code -> discountService.findPromoCode(code) }
        if (promoCodeDiscount != null) realizedProductBuilders.forEach { it.applyDiscount(promoCodeDiscount) }

        return realizedProductBuilders.map(RealizedProduct.Builder::build).toList()
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun registerOrder(@Valid request: Order.RegisterRequest): Order {
        if (request.products.values.contains { it <= 0 }) throw makeMemorizedException("Product quantity must be >= 1")

        val productsWithQuantity = productService // products and requested quantity
            .findProductsByIdsOrThrow(request.products.keys)
            .associateWith(request::findProductQuantity)

        for ((product, quantity) in productsWithQuantity)
            if (!product.haveEnoughQuantity(quantity))
                throw FastException("Product ${product.id} don`t have enough quantity (only available ${product.availableQuantity})")

        val realizedProducts = processDiscounts(productsWithQuantity, request.promoCode)

        if (realizedProducts.size != request.products.size) throw makeMemorizedException("Can`t process all requested products")

        return orderRepo.save(
            Order(
                products = realizedProducts,
                owner = accountService.getAuthenticatedAccount().orThrow(),
                status = OrderStatus.UNPAID,
                shippingAddress = request.shippingAddress
            )
        ).also { order ->
            eventPublisher.publishEvent(Event.OrderRegister(order, this))
        }
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun listMyOrders(pageable: CheckedPageable): Page<Order> {
        return orderRepo.findAllByOwnerUsername(authenticatedPrincipal().name, pageable)
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun countMyOrders(): Long {
        return orderRepo.countByOwnerUsername(authenticatedPrincipal().name)
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

