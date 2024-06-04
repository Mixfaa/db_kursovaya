package com.mixfa.marketplace.marketplace.service


import com.mixfa.account.service.AccountService
import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.excify.FastException
import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.service.repo.OrderRepository
import com.mixfa.shared.*
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class OrderService(
    private val orderRepo: OrderRepository,
    private val accountService: AccountService,
    private val discountService: DiscountService,
    private val eventPublisher: ApplicationEventPublisher,
    private val mongoTemplate: MongoTemplate,
) {

    private fun processDiscounts(products: Map<Product, Long>, promoCode: String?): List<RealizedProduct> {
        val realizedProductBuilders =
            products.asSequence().map { (product, quantity) -> RealizedProduct.Builder(product, quantity) }

        val promoCodeDiscount = promoCode?.let { code -> discountService.findPromoCode(code) }
        if (promoCodeDiscount != null) realizedProductBuilders.forEach { it.applyDiscount(promoCodeDiscount) }

        return realizedProductBuilders.map(RealizedProduct.Builder::build).toList()
    }

    @PreAuthorize("hasAuthority('ORDER:EDIT')")
    fun registerOrder(orderData: OrderBuilder.WithOrderData): Order {
        if (orderData.products.values.contains { it <= 0 }) throw makeMemorizedException("Product quantity must be >= 1")

        for ((product, quantity) in orderData.products)
            if (!product.haveEnoughQuantity(quantity))
                throw FastException("Product ${product.id} don`t have enough quantity (only available ${product.availableQuantity})")

        val realizedProducts = processDiscounts(orderData.products, orderData.promoCode)

        if (realizedProducts.size != orderData.products.size) throw makeMemorizedException("Can`t process all requested products")

        return orderRepo.save(
            Order(
                products = realizedProducts,
                owner = accountService.getAuthenticatedAccount().orThrow(),
                status = OrderStatus.UNPAID,
                shippingAddress = orderData.shippingAddress
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
    fun changeOrderStatus(orderId: String, newStatus: OrderStatus) {
        mongoTemplate.updateFirst(
            Query(Criteria.where("_id").`is`(orderId)),
            Update.update(fieldName(Order::status), newStatus.name),
            ORDER_MONGO_COLLECTION
        )
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class OrderRegister(val order: Order, src: Any) : Event(src)
        class OrderCancel(val order: Order, src: Any) : Event(src)
    }
}

