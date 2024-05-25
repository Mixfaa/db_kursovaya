package com.mixfa.marketplace.marketplace.model

import com.mixfa.shared.defaultLazy
import com.mixfa.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

const val CATEGORY_MONGO_COLLECTION = "category"

@Document(CATEGORY_MONGO_COLLECTION)
data class Category(
    @Id val name: String,
    val parentCategoryId: String?,
    val subcategoriesIds: Set<String>,
    val requiredProps: Set<String>
) : WithDto {
    data class RegisterRequest(
        @field:NotBlank
        val name: String,
        val parentCategory: String? = null,
        @field:NotNull
        val requiredProps: Set<String>,
    )

    @delegate:Transient
    override val asDto: Dto by defaultLazy { Dto(this) }

    data class Dto(
        val name: String,
        val subcategories: Set<String>,
        val requiredProps: Set<String>
    ) {
        constructor(category: Category) : this(
            category.name,
            category.subcategoriesIds,
            category.requiredProps
        )
    }

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