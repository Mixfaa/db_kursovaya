package com.mixfa.marketplace.marketplace.model.discount

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document("discount")
abstract class AbstractDiscount(
    val description: String,
    val discount: Double,
    @field:Id val id: ObjectId = ObjectId()
) {
    @get:JsonIgnore
    @get:Transient
    val multiplier: Double
        get() = discount / 100.0

    sealed class AbstractRegisterRequest(
        @NotBlank
        val description: String,
        @NotNull
        val discount: Double,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractDiscount) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}