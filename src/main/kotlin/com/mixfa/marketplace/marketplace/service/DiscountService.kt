package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import com.mixfa.marketplace.marketplace.model.discount.DiscountByCategory
import com.mixfa.marketplace.marketplace.model.discount.DiscountByProduct
import com.mixfa.marketplace.marketplace.model.discount.PromoCode
import com.mixfa.marketplace.marketplace.service.repo.DiscountRepository
import com.mixfa.marketplace.shared.event.MarketplaceEvent
import com.mixfa.marketplace.shared.iteratePages
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.orThrow
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service


@Service
class DiscountService(
    private val discountRepo: DiscountRepository,
    private val categoryService: CategoryService,
    private val productService: ProductService,
    private val publisher: ApplicationEventPublisher,
) : ApplicationListener<ProductService.Event> {
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerDiscount(request: AbstractDiscount.AbstractRegisterRequest): AbstractDiscount = when (request) {
        is DiscountByProduct.RegisterRequest -> {
            val targetProducts = productService.findProductsByIdsOrThrow(request.targetProductsIds)

            DiscountByProduct(
                description = request.description,
                discount = request.discount,
                targetProducts = targetProducts
            )
        }

        is DiscountByCategory.RegisterRequest -> {
            val targetCategories = categoryService.findCategoriesByIdOrThrow(request.targetCategoriesIds)

            DiscountByCategory(
                description = request.description,
                discount = request.discount,
                targetCategories = targetCategories
            )
        }

        is PromoCode.RegisterRequest -> PromoCode(request.code, request.description, request.discount)
    }
        .let(discountRepo::save)
        .also { discount ->
            val event = Event.DiscountRegister(discount, this)
            publisher.publishEvent(event)
        }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteDiscount(discountId: String) {
        val discount = discountRepo.findById(discountId).orThrow()
        val event = Event.DiscountDelete(discount, this)
        publisher.publishEvent(event)

        discountRepo.deleteById(discountId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    private fun handleProductDeletion(product: Product) {
        processAllDiscounts { discount ->
            if (discount is DiscountByProduct) {

                if (discount.targetProducts.contains(product))
                    discountRepo.save(
                        DiscountByProduct(
                            discount.description,
                            discount.discount,
                            discount.targetProducts - product
                        )
                    )
            }
        }
    }

    fun processAllDiscounts(handler: (AbstractDiscount) -> Unit) = iteratePages(discountRepo::findAll, handler)

    fun listDiscounts(pageable: CheckedPageable) = discountRepo.findAll(pageable)

    override fun onApplicationEvent(event: ProductService.Event) = when (event) {
        is ProductService.Event.ProductDelete -> handleProductDeletion(event.product)
        else -> {}
    }

    sealed class Event(src: Any) : MarketplaceEvent(src) {
        class DiscountRegister(val discount: AbstractDiscount, src: Any) : Event(src)
        class DiscountDelete(val discount: AbstractDiscount, src: Any) : Event(src)
    }
}

