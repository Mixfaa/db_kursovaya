package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByCategory(
    description: String,
    discount: Double,
    @DBRef val targetCategories: List<Category>,
    id: ObjectId = ObjectId()
) : AbstractDiscount(description, discount, id) {
    class RegisterRequest(
        description: String,
        discount: Double,
        @field:NotEmpty
        val targetCategoriesIds: List<String>
    ) : AbstractRegisterRequest(description, discount)

    fun isApplicableTo(product: Product) = checkCategoriesIntersections(product.categories, targetCategories)

    fun buildCategoriesSet(): Set<Category> {
        fun addCategories(set: MutableSet<Category>, categories: List<Category>) {
            set.addAll(categories)

            for (category in categories) {
                category.parentCategory?.let(set::add)
                addCategories(set, category.subcategories)
            }
        }

        return buildSet {
            addCategories(this, targetCategories)
        }
    }
}

private fun checkCategoriesIntersections(
    productCategories: List<Category>, discountCategories: List<Category>
): Boolean {
    for (discountCategory in discountCategories)
        for (productCategory in productCategories)
            if (discountCategory == productCategory) return true

    for (discountCategory in discountCategories)
        if (checkCategoriesIntersections(
                productCategories,
                discountCategory.subcategories
            )
        ) return true

    return false
}