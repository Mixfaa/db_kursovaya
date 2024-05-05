package com.mixfa.marketplace.marketplace.model.discount

import com.mixfa.marketplace.marketplace.model.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.DBRef

class DiscountByProduct(
    description: String,
    discount: Double,
    @DBRef val targetProducts: List<Product>,
    id: ObjectId = ObjectId()
) : AbstractDiscount(description, discount, id) {
    class RegisterRequest(
        @NotBlank
        description: String,
        @NotNull
        discount: Double,
        @NotEmpty
        val targetProductsIds: List<String>
    ) : AbstractRegisterRequest(description, discount)

    fun isApplicableTo(product: Product) = targetProducts.contains(product)
}