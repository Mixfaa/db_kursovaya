package com.mixfa.marketplace.marketplace.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document("category")
data class Category(
    @Id val name: String,
    @field:DBRef val parentCategory: Category?,
    @field:DBRef val subcategories: List<Category>,
    val requiredProps: List<String>
) {
    data class CreateRequest(
        @NotBlank
        val name: String,
        @NotNull
        val requiredProps: List<String>,
        val subcategories: List<String>? = null,
        val parentCategory: String? = null
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}