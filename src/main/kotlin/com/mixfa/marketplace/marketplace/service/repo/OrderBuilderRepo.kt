package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.OrderBuilder
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderBuilderRepo : MongoRepository<OrderBuilder, String> {
    fun findByOwnerUsername(username: String): OrderBuilder?
}
