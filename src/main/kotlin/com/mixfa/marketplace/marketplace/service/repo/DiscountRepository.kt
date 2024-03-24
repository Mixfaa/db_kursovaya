package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface DiscountRepository : MongoRepository<AbstractDiscount, String> {
    override fun findAll(pageable: Pageable): Page<AbstractDiscount>
}