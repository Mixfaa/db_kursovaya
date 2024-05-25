package com.mixfa.marketplace.marketplace.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mixfa.account.model.Account
import com.mixfa.shared.defaultLazy
import com.mixfa.shared.model.WithDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

const val ORDER_BUILDER_COLLECTION = "order-builder"

@Document(ORDER_BUILDER_COLLECTION)
data class OrderBuilder(
    @Id val id: ObjectId = ObjectId(),
    @field:DBRef val owner: Account,
    val productsIds: Map<String, Long>
) : WithDto {
    @delegate:JsonIgnore
    @delegate:Transient
    override val asDto: Dto by defaultLazy { Dto(this) }

    data class WithOrderData(
        val orderBuilder: OrderBuilder,
        val products: Map<Product, Long>,
        val shippingAddress: String,
        val promoCode: String? = null,
    ) {
        val owner: Account by orderBuilder::owner
    }

    data class Dto(
        val id: String,
        val ownerId: String,
        val products: Map<String, Long>
    ) {
        constructor(orderBuilder: OrderBuilder) : this(
            orderBuilder.id.toString(),
            orderBuilder.owner.username,
            orderBuilder.productsIds
        )
    }
}
