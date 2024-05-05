package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface CategoryRepository : MongoRepository<Category, String> {
    override fun findAll(pageable: Pageable): Page<Category>
    fun findAllByNameContainsIgnoreCase(query: String, pageable: Pageable): Page<Category>

    @Query("{ \$text :  {\$search: ':#{#query}'}}")
    fun findByText(query: String, pageable: Pageable): Page<Category>
}