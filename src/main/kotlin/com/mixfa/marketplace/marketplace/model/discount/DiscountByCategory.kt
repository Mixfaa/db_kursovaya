package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import jakarta.validation.constraints.NotEmpty
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByCategory(
    description: String,
    discount: Double,
    @field:DBRef val targetCategories: List<Category>
) : AbstractDiscount(description, discount) {
    class RegisterRequest(
        description: String,
        discount: Double,
        @field:NotEmpty
        val targetCategoriesIds: List<String>
    ) : AbstractRegisterRequest(description, discount)

    fun isApplicableTo(product: Product) = checkCategoriesIntersections(product.categories, targetCategories)
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