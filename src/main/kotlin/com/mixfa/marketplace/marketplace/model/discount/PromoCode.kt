package com.mixfa.marketplace.marketplace.model.discount

import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId

class PromoCode(
    val code: String,
    description: String,
    discount: Double,
    id: ObjectId = ObjectId()
) : AbstractDiscount(description, discount, id) {
    class RegisterRequest(
        @field:NotBlank
        val code: String,
        description: String,
        discount: Double
    ) : AbstractRegisterRequest(description, discount)
}