package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.account.model.Account
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document("FavouriteList")
data class FavouriteList(
    val id: ObjectId = ObjectId(),
    val name: String,
    @field:DBRef val owner: Account,
    @field:DBRef val products: List<Product>,
    val isPublic: Boolean
) {
    data class RegisterRequest(
        @NotBlank
        val name: String,
        @NotNull
        val isPublic: Boolean,
        val productsIds: List<String>? = null
    )

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