package com.mixfa.marketplace.marketplace.repository

import com.mixfa.marketplace.marketplace.model.OrderBuilder
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface OrderBuilderRepo : MongoRepository<OrderBuilder, String> {
    fun findByOwnerUsername(username: String): OrderBuilder?
    fun existsByOwnerUsername(username: String) : Boolean
}
