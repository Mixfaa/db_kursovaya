package com.mixfa.marketplace.marketplace.model

import com.mixfa.account.model.Account
import com.mixfa.shared.defaultLazy
import com.mixfa.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

const val ORDER_MONGO_COLLECTION = "order"

@Document(ORDER_MONGO_COLLECTION)
data class Order(
    @Id val id: ObjectId = ObjectId(),
    val products: List<RealizedProduct>,
    @field:DBRef val owner: Account,
    val status: OrderStatus,
    val shippingAddress: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
) : WithDto {
    data class RegisterRequest(
        @field:NotEmpty
        val products: Map<String, Long>, // product id to quantity
        @field:NotBlank
        val shippingAddress: String,
        val promoCode: String? = null,
    )

    @delegate:Transient
    override val asDto: Dto by defaultLazy { Dto(this) }

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

fun Order.RegisterRequest.findProductQuantity(product:Product) = this.products[product.id.toString()] ?: throw NoSuchElementException()