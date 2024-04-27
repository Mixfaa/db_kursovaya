package com.mixfa.marketplace.marketplace.model

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.mixfa.marketplace.shared.model.WithDto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document("category")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
data class Category(
    @Id val name: String,
    @field:DBRef val parentCategory: Category?,
    @field:DBRef val subcategories: List<Category>,
    val requiredProps: List<String>
) : WithDto {
    data class RegisterRequest(
        @NotBlank
        val name: String,
        @NotNull
        val requiredProps: List<String>,
        val subcategories: List<String>? = null,
        val parentCategory: String? = null
    )

    @get:JsonIgnore
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val name: String,
        val parentCategory: String?,
        val subcategories: List<String>,
            val requiredProps: List<String>
    ) {
        constructor(category: Category) : this(
            category.name,
            category.parentCategory?.name,
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