package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.account.model.Account
import org.bson.types.ObjectId
import org.hibernate.validator.constraints.Range
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime

@Document("comment")
data class Comment(
    val id: ObjectId = ObjectId(),
    @field:DBRef val owner: Account,
    @field:DBRef val product: Product,
    val content: String,
    val rate: Double,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
) {
    data class RegisterRequest(
        val productId: String,
        val content: String,
        @Range(min = 0L, max = 5L)
        val rate: Double
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}