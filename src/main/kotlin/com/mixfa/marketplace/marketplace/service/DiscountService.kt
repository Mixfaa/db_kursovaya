package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.model.discount.*
import com.mixfa.marketplace.marketplace.service.repo.DiscountRepository
import com.mixfa.shared.findIterating
import com.mixfa.shared.model.CheckedPageable
import com.mixfa.shared.model.MarketplaceEvent
import com.mixfa.shared.orThrow
import jakarta.validation.Valid
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class DiscountService(
    private val discountRepo: DiscountRepository,
    private val categoryService: CategoryService,
    private val productService: ProductService,
    private val publisher: ApplicationEventPublisher,
    private val mongoTemplate: MongoTemplate,
) : ApplicationListener<ProductService.Event> {

    private fun buildCategoriesIdsSet(targetCategories: List<Category>): List<String> {
        fun addCategories(set: MutableList<String>, categories: List<Category>) {
            categories.forEach { set.add(it.name) }

            for (category in categories) {
                category.parentCategoryId?.let { id ->
                    val parentCategory = categoryService.findCategoryById(id).orThrow()
                    set.add(parentCategory.name)
                }
                addCategories(set, category.subcategoriesIds.map { id ->
                    categoryService.findCategoryById(id).orThrow()
                })
            }
        }

        return buildList {
            addCategories(this, targetCategories)
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerDiscount(@Valid request: AbstractDiscount.AbstractRegisterRequest): AbstractDiscount = when (request) {
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
                allCategoriesIds = buildCategoriesIdsSet(targetCategories)
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

        publisher.publishEvent(Event.DiscountDelete(discount, this))
        discountRepo.deleteById(discountId)
    }

    fun findPromoCode(code: String): PromoCode? {
        return mongoTemplate.findOne(
            Query(Criteria.where(PromoCode::code.name).`is`(code)),
            PromoCode::class.java,
            DISCOUNT_MONGO_COLLECTION
        )
    }

    private fun handleProductDeletion(product: Product) {
        val discounts = mongoTemplate.findIterating<DiscountByProduct>(
            Query(Criteria.where(DiscountByProduct::targetProducts.name).`in`(product)),
            DISCOUNT_MONGO_COLLECTION
        )

        for (discount in discounts)
            discountRepo.save(
                DiscountByProduct(
                    discount.description,
                    discount.discount,
                    discount.targetProducts - product
                )
            )
    }

    fun findDiscounts(query: String, pageable: CheckedPageable) =
        discountRepo.findAllByDescriptionContainingIgnoreCase(query, pageable)

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

