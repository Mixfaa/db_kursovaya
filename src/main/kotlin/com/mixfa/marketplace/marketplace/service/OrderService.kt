package com.mixfa.marketplace.marketplace.service

import com.mixfa.excify.FastThrowable
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.model.discount.DiscountByCategory
import com.mixfa.marketplace.marketplace.model.discount.DiscountByProduct
import com.mixfa.marketplace.marketplace.model.discount.PromoCode
import com.mixfa.marketplace.marketplace.service.repo.OrderRepository
import com.mixfa.marketplace.shared.*
import com.mixfa.marketplace.shared.event.MarketplaceEvent
import com.mixfa.marketplace.shared.model.CheckedPageable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepo: OrderRepository,
    private val accountService: AccountService,
    private val discountService: DiscountService,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher
) {
    fun calculateOrderCost(request: Order.RegisterRequest): TempOrder {
        val products = productService.findProductsByIdsOrThrow(request.products)
        return TempOrder(processDiscounts(products, request.promoCode))
    }

    private fun processDiscounts(products: List<Product>, promoCode: String?): List<RealizedProduct> {
        val realizedProductBuilders = products.asSequence().map { RealizedProduct.Builder(it) }

        discountService.processAllDiscounts { discount ->
            when (discount) {
                is DiscountByProduct -> {
                    realizedProductBuilders.forEach { builder ->
                        if (discount.targetProducts.contains(builder.product)) builder.applyDiscount(discount)
                    }
                }

                is DiscountByCategory -> {
                    realizedProductBuilders.forEach { builder ->
                        if (checkCategoriesIntersections(
                                builder.product.categories,
                                discount.targetCategories
                            )
                        ) builder.applyDiscount(discount)
                    }
                }

                is PromoCode -> {
                    if (promoCode == null) return@processAllDiscounts

                    if (discount.matches(promoCode)) realizedProductBuilders.forEach { builder ->
                        builder.applyDiscount(discount)
                    }
                }
            }
        }

        return realizedProductBuilders.map { it.build() }.toList()
    }

    @Transactional
    @PreAuthorize("isAuthenticated() == true")
    fun registerOrder(request: Order.RegisterRequest): Order {
        val account = accountService.getAuthenticatedAccount().orThrow()

        val products = productService.findProductsByIdsOrThrow(request.products)

        if (products.size != request.products.size) throw FastThrowable("Can`t load all requested products")

        // apply discounts and promocode
        val realizedProducts = processDiscounts(products, request.promoCode)

        if (realizedProducts.size != request.products.size) throw FastThrowable("Can`t process all requested products")

        return orderRepo.save(
            Order(
                products = realizedProducts,
                owner = account,
                status = OrderStatus.UNPAID,
                shippingAddress = request.shippingAddress
            )
        ).also {
            eventPublisher.publishEvent(Event.OrderRegister(it, this))
        }
    }

    @PreAuthorize("isAuthenticated() == true")
    fun listMyOrders(pageable: CheckedPageable): Page<Order> {
        val principal = SecurityUtils.getAuthenticatedPrincipal()
        return orderRepo.findAllByOwnerEmail(principal.name, pageable)
    }

    @PreAuthorize("isAuthenticated() == true")
    fun countMyOrders(): Long {
        val principal = SecurityUtils.getAuthenticatedPrincipal()
        return orderRepo.countByOwnerEmail(principal.name)
    }

    @PreAuthorize("isAuthenticated() == true")
    fun cancelOrder(orderId: String): Order {
        val principal = SecurityUtils.getAuthenticatedPrincipal()
        val order = orderRepo.findById(orderId).orThrow()

        principal.throwIfNot(order.owner)

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

private fun checkCategoriesIntersections(
    productCategories: List<Category>, discountCategories: List<Category>
): Boolean {

    for (discountCategory in discountCategories) for (productCategory in productCategories) if (discountCategory == productCategory) return true

    for (discountCategory in discountCategories) if (checkCategoriesIntersections(
            productCategories,
            discountCategory.subcategories
        )
    ) return true

    return false
}