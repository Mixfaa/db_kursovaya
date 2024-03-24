package com.mixfa.marketplace.marketplace.model.discount

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

class PromoCode(
    private val code: String,
    description: String,
    discount: Double
) : AbstractDiscount(description, discount) {
    fun matches(code: String) = this.code == code

    class RegisterRequest(
        @NotBlank
        val code: String,
        @NotBlank
        description: String,
        @NotNull
        @Range(min = 1, max = 100)
        discount: Double
    ) : AbstractRegisterRequest(description, discount)
}