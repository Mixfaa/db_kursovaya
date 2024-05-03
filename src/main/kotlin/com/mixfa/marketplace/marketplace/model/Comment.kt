package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId
import org.hibernate.validator.constraints.Range
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.ZonedDateTime
import java.util.Date

@Document("comment")
data class Comment(
    val id: ObjectId = ObjectId(),
    @field:DBRef val owner: Account,
    @field:DBRef val product: Product,
    val content: String,
    val rate: Double,
    val timestamp: Date
) : WithDto {
    data class RegisterRequest(
        @field:NotBlank
        val productId: String,
        @field:NotBlank
        val content: String,
        @field:Range(min = 0L, max = 5L)
        val rate: Double
    )

    @delegate:Transient
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val id: String,
        val ownerId: String,
        val productId: String,
        val content: String,
        val rate: Double,
        val timestamp: Date
    ) {
        constructor(comment: Comment) : this(
            comment.id.toString(),
            comment.owner.username,
            comment.product.id.toString(),
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