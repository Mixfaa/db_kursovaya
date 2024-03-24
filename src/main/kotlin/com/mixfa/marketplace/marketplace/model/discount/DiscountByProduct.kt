package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByProduct(
    description: String,
    discount: Double,
    @field:DBRef val targetProducts: List<Product>
) : AbstractDiscount(description, discount) {
    class RegisterRequest(
        @NotBlank
        description: String,
        @NotNull
        discount: Double,
        @NotEmpty
        val targetProductsIds: List<String>
    ) : AbstractRegisterRequest(description, discount)
}