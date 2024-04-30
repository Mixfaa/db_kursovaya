package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.service.repo.CategoryRepository
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.categoryNotFound
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.orThrow
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class CategoryService(
    private val categoryRepo: CategoryRepository
) {
    fun findCategoriesByIdOrThrow(ids: List<String>): List<Category> {
        if (ids.isEmpty()) return emptyList()
        val categories = categoryRepo.findAllById(ids)
        if (categories.size != ids.size) throw NotFoundException.categoryNotFound()
        return categories
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerCategory(@Valid request: Category.RegisterRequest): Category = categoryRepo.save(
        Category(
            name = request.name,
            subcategories = request.subcategories?.let(::findCategoriesByIdOrThrow) ?: emptyList(),
            parentCategory = request.parentCategory?.let { categoryRepo.findById(it).orThrow() },
            requiredProps = request.requiredProps,
        )
    )

    fun findCategories(query: String, pageable: CheckedPageable): Page<Category> =
        categoryRepo.findAllByNameContainsIgnoreCase(query, pageable)

    fun listCategories(pageable: CheckedPageable): Page<Category> =
        categoryRepo.findAll(pageable)

    fun countCategories() = categoryRepo.count()
}