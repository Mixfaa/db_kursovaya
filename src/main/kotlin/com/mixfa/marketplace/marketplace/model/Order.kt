package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.account.model.Account
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
) {
    data class RegisterRequest(
        val products: List<String>,
        val shippingAddress: String,
        val promoCode: String? = null,
    )
}

data class TempOrder(
    val products: List<RealizedProduct>
)