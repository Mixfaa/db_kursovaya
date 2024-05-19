package com.mixfa.marketplace.marketplace.model

import com.mixfa.shared.defaultLazy
import com.mixfa.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

const val PRODUCT_MONGO_COLLECTION = "product"

@Document(PRODUCT_MONGO_COLLECTION)
data class Product(
    @Id val id: ObjectId = ObjectId(),
    @TextIndexed val caption: String,
    @DBRef val categories: Set<Category>,
    val characteristics: Map<String, String>,
    @TextIndexed val description: String,
    val price: Double,
    val actualPrice: Double = price,
    val rate: Double = 0.0,
    val ordersCount: Long = 0,
    val availableQuantity: Long,
    val images: List<String>,
) : WithDto {
    @delegate:Transient
    override val asDto: Dto by defaultLazy { Dto(this) }

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
        val availableQuantity: Long,
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
            product.availableQuantity,
            product.images,
        )
    }

    data class RegisterRequest(
        @field:NotBlank
        val caption: String,
        @field:NotEmpty
        val categories: List<String>,
        @field:NotNull
        @field:NotEmpty
        val characteristics: Map<String, String>,
        @field:NotBlank
        val description: String,
        @field:NotNull
        val price: Double,
        val availableQuantity: Long,
        val images: List<String>
    )

    fun haveEnoughQuantity(orderQuantity: Long) : Boolean = availableQuantity >= orderQuantity

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
