package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface CategoryRepository : MongoRepository<Category, String> {
    override fun findAll(pageable: Pageable): Page<Category>
    fun findAllByNameContainsIgnoreCase(query: String, pageable: Pageable): Page<Category>
}