package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.service.repo.CategoryRepository
import com.mixfa.marketplace.shared.CheckedPageable
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.orThrow
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepo: CategoryRepository
) {
    fun findCategoriesByIdOrThrow(ids: List<String>): List<Category> {
        val categories = categoryRepo.findAllById(ids)
        if (categories.size != ids.size) throw NotFoundException("Some category")
        return categories
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun registerCategory(request: Category.RegisterRequest): Category = categoryRepo.save(
        Category(
            name = request.name,
            subcategories = request.subcategories?.let(::findCategoriesByIdOrThrow) ?: emptyList(),
            requiredProps = request.requiredProps,
            parentCategory = request.parentCategory?.let { categoryRepo.findById(it).orThrow() }
        )
    )

    fun findCategories(query: String, pageable: CheckedPageable): Page<Category> {
        return categoryRepo.findAllByNameContainsIgnoreCase(query, pageable)
    }

    fun listCategories(pageable: CheckedPageable): Page<Category> {
        return categoryRepo.findAll(pageable)
    }

    fun countCategories() = categoryRepo.count()
}