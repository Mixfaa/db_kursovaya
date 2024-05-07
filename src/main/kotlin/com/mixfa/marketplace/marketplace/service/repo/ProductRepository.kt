package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.Category
import com.mixfa.marketplace.marketplace.model.Product
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ProductRepository : MongoRepository<Product, String> {
    fun findAllByCaptionContainingIgnoreCase(query: String, pageable: Pageable): Page<Product>

    @Query("{ \$text :  {\$search: ':#{#query}'}}")
    fun findAllByText(query: String, pageable: Pageable): Page<Product>

    fun findAllByCategoriesContains(categories: Set<Category>) : List<Product>
    fun findAllByIdIn(ids: List<ObjectId>) : List<Product>
}