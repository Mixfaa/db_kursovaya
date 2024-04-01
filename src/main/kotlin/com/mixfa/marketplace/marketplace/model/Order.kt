package com.mixfa.marketplace.marketplace.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.shared.model.WithDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document("order")
data class Order(
    @Id val id: ObjectId = ObjectId(),
    val products: List<RealizedProduct>,
    @field:DBRef val owner: Account,
    val status: OrderStatus,
    val shippingAddress: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
) : WithDto {
    data class RegisterRequest(
        val products: List<String>,
        val shippingAddress: String,
        val promoCode: String? = null,
    )

    @get:JsonIgnore
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val id: String,
        val products: List<RealizedProduct>,
        val ownerId: String,
        val status: OrderStatus,
        val shippingAddress: String,
        val timestamp: ZonedDateTime
    ) {
        constructor(order: Order) : this(
            order.id.toString(),
            order.products,
            order.owner.username,
            order.status,
            order.shippingAddress,
            order.timestamp
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class TempOrder(
    val products: List<RealizedProduct>
)