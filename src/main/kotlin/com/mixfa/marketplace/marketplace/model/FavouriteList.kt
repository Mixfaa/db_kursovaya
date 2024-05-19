package com.mixfa.marketplace.marketplace.model

import com.mixfa.account.model.Account
import com.mixfa.shared.defaultLazy
import com.mixfa.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

const val FAVLIST_MONGO_COLLECTION = "favourite_list"

@Document(FAVLIST_MONGO_COLLECTION)
data class FavouriteList(
    val id: ObjectId = ObjectId(),
    val name: String,
    @field:DBRef val owner: Account,
    @field:DBRef val products: List<Product>,
    val isPublic: Boolean
) : WithDto {
    data class RegisterRequest(
        @field:NotBlank
        val name: String,
        @field:NotNull
        val isPublic: Boolean,
        val productsIds: List<String>? = null
    )

    @delegate:Transient
    override val asDto: Dto by defaultLazy { Dto(this) }

    data class Dto(
        val id: String,
        val name: String,
        val ownerId: String,
        val products: List<Product.Dto>,
        val isPublic: Boolean
    ) {
        constructor(list: FavouriteList) : this(
            list.id.toString(),
            list.name,
            list.owner.username,
            list.products.map(Product::asDto),
            list.isPublic
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavouriteList

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}