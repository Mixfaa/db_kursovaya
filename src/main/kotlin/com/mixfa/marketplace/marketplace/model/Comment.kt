package com.mixfa.marketplace.marketplace.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.shared.WithDto
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
) : WithDto<Comment.Dto> {
    data class RegisterRequest(
        val productId: String,
        val content: String,
        @Range(min = 0L, max = 5L)
        val rate: Double
    )

    @get:JsonIgnore
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val id: String,
        val ownerId: String,
        val product: Product.Dto,
        val content: String,
        val rate: Double,
        val timestamp: ZonedDateTime
    ) {
        constructor(comment: Comment) : this(
            comment.id.toString(),
            comment.owner.username,
            comment.product.asDto,
            comment.content,
            comment.rate,
            comment.timestamp
        )
    }

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