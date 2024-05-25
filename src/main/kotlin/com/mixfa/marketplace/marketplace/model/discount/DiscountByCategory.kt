package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Category
import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByCategory(
    description: String,
    discount: Double,
    @DBRef val targetCategories: List<Category>,
    val allCategoriesIds: List<String>,
    id: ObjectId = ObjectId()
) : AbstractDiscount(description, discount, id) {
    class RegisterRequest(
        description: String,
        discount: Double,
        @field:NotEmpty
        val targetCategoriesIds: List<String>
    ) : AbstractRegisterRequest(description, discount)
}
