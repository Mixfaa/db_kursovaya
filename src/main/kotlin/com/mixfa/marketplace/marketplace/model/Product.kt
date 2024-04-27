package com.mixfa.marketplace.marketplace.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mixfa.marketplace.shared.model.WithDto
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
    val ordersCount: Long = 0,
    val images: List<String>,
    val actualPrice: Double = price,
) : WithDto {
    data class RegisterRequest(
        @NotBlank
        val caption: String,
        @NotEmpty
        val categories: List<String>,
        @NotNull
        val characteristics: Map<String, String>,
        @NotBlank
        val description: String,
        @NotNull
        val price: Double,
        val images: List<String>
    )

    @get:JsonIgnore
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val id: String,
        val caption: String,
        val categories: List<String>,
        val characteristics: Map<String, String>,
        val description: String,
        val price: Double,
        val actualPrice: Double,
        val rate: Double,
        val ordersCount: Long,
        val images: List<String>,
    ) {
        constructor(product: Product) : this(
            product.id.toString(),
            product.caption,
            product.categories.map(Category::name),
            product.characteristics,
            product.description,
            product.price,
            product.actualPrice,
            product.rate,
            product.ordersCount,
            product.images
        )
    }

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
