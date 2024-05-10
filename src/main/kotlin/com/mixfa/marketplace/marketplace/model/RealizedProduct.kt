package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import org.bson.types.ObjectId

data class RealizedProduct(
    val caption: String,
    val description: String,
    val productId: ObjectId,
    val quantity: Long,
    val price: Double
) {
    constructor(product: Product, quantity: Long) : this(
        product.caption, product.description, product.id, quantity, product.price
    )

    data class Builder(
        var caption: String,
        var description: String,
        var price: Double,
        val quantity: Long,
        val product: Product
    ) {
        constructor(product: Product, quantity: Long) : this(
            product.caption, product.description, product.actualPrice, quantity, product
        )

        fun applyDiscount(discount: AbstractDiscount) {
            price *= discount.multiplier
        }

        fun build() = RealizedProduct(caption, description, product.id, quantity, price)
    }
}