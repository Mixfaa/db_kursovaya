package com.mixfa.marketplace.marketplace.model.discount

import jakarta.validation.constraints.NotBlank

class PromoCode(
    val code: String,
    description: String,
    discount: Double
) : AbstractDiscount(description, discount) {
    class RegisterRequest(
        @field:NotBlank
        val code: String,
        description: String,
        discount: Double
    ) : AbstractRegisterRequest(description, discount)
}