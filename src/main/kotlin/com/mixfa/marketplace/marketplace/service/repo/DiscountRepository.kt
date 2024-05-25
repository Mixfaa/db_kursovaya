package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface DiscountRepository : MongoRepository<AbstractDiscount, String> {
    override fun findAll(pageable: Pageable): Page<AbstractDiscount>

//    @Query("{ \$text : {\$search: ':#{#query}'}}")
//    fun findByText(query: String, pageable: Pageable): Page<AbstractDiscount>

    fun findAllByDescriptionContainingIgnoreCase(query: String, pageable: Pageable) : Page<AbstractDiscount>
}