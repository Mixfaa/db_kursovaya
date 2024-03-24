package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderRepository : MongoRepository<Order, String> {
    fun findAllByOwnerEmail(email: String, pageable: Pageable): Page<Order>
    fun countByOwnerEmail(email: String): Long
}