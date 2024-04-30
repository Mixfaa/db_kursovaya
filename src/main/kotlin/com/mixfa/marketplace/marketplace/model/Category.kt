package com.mixfa.marketplace.marketplace.model

import com.mixfa.marketplace.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document("category")
data class Category(
    @Id val name: String,
    @DBRef val parentCategory: Category?,
    @DBRef val subcategories: List<Category>,
    val requiredProps: List<String>
) : WithDto {
    data class RegisterRequest(
        @NotBlank
        val name: String,
        val parentCategory: String? = null,
        @NotNull
        val requiredProps: List<String>,
        val subcategories: List<String>? = null,
    )

    @delegate:Transient
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val name: String,
        val subcategories: List<String>,
        val requiredProps: List<String>
    ) {
        constructor(category: Category) : this(
            category.name,
            category.subcategories.map(Category::name),
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