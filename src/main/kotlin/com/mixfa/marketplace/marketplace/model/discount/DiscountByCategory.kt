package com.mixfa.marketplace.marketplace.model.discount

import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId

class DiscountByCategory(
    description: String,
    discount: Double,
    val allCategoriesIds: List<ObjectId>,
    id: ObjectId = ObjectId()
) : AbstractDiscount(description, discount, id) {
    class RegisterRequest(
        description: String,
        discount: Double,
        @field:NotEmpty
        val targetCategoriesIds: Set<String>
    ) : AbstractRegisterRequest(description, discount)
}
