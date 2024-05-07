package com.mixfa.marketplace.marketplace.model.discount

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document

const val DISCOUNT_MONGO_COLLECTION = "discount"

@Document(DISCOUNT_MONGO_COLLECTION)
abstract class AbstractDiscount(
    @TextIndexed val description: String,
    val discount: Double,
    @field:Id val id: ObjectId = ObjectId()
) {
    @get:JsonIgnore
    @get:Transient
    val multiplier: Double
        get() = 1.0 - (discount / 100.0)

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes(
        JsonSubTypes.Type(value = PromoCode.RegisterRequest::class),
        JsonSubTypes.Type(value = DiscountByProduct.RegisterRequest::class),
        JsonSubTypes.Type(value = DiscountByProduct.RegisterRequest::class)
    )
    sealed class AbstractRegisterRequest(
        @field:NotBlank
        val description: String,
        @field:NotNull
        @field:Max(100)
        @field:Min(0)
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