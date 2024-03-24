package com.mixfa.marketplace.marketplace.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document("product")
data class Product(
    @Id val id: ObjectId = ObjectId(),
    val caption: String,
    @field:DBRef val categories: List<Category>,
    val characteristics: Map<String, String>,
    val description: String,
    val price: Double,
    val rate: Double = 0.0,
    val ordersCount: Long = 0
) {
    data class CreateRequest(
        @NotBlank
        val caption: String,
        @NotEmpty
        val categories: List<String>,
        @NotNull
        val characteristics: Map<String, String>,
        @NotBlank
        val description: String,
        @NotNull
        val price: Double
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
