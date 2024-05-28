package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.account.model.Account
import com.mixfa.marketplace.marketplace.model.Comment
import com.mixfa.marketplace.marketplace.model.Product
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface CommentRepository : MongoRepository<Comment, String> {
    fun findAllByProductId(id: ObjectId, pageable: Pageable): Page<Comment>
    fun deleteAllByProduct(product: Product)
    fun findAllByOwnerUsername(username: String, pageable: Pageable) : Page<Comment>
}