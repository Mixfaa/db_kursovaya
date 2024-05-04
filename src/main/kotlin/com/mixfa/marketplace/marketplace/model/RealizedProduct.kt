package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import org.bson.types.ObjectId

data class RealizedProduct(
    val caption: String,
    val description: String,
    val productId: ObjectId,
    val price: Double
) {
    constructor(product: Product) : this(
        product.caption, product.description, product.id, product.price
    )

    data class Builder(
        var caption: String,
        var description: String,
        var price: Double,
        val product: Product
    ) {
        constructor(product: Product) : this(
            product.caption, product.description, product.actualPrice, product
        )

        fun applyDiscount(discount: AbstractDiscount) {
            price *= discount.multiplier
        }

        fun build() = RealizedProduct(caption, description, product.id, price)
    }
}