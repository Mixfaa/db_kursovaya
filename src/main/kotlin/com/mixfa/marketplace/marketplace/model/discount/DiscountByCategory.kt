package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Category
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByCategory(
    description: String,
    discount: Double,
    @field:DBRef val targetCategories: List<Category>
) : AbstractDiscount(description, discount) {
    class RegisterRequest(
        @NotBlank
        description: String,
        @NotNull
        discount: Double,
        @NotEmpty
        val targetCategoriesIds: List<String>
    ) : AbstractRegisterRequest(description, discount)
}