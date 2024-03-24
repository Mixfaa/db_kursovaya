package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.marketplace.model.Comment
import com.mixfa.marketplace.marketplace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface CommentRepository : MongoRepository<Comment, String> {
    fun findAllByProduct(product: Product, pageable: Pageable): Page<Comment>
    fun findAllByProductId(id: String, pageable: Pageable): Page<Comment>

    fun deleteAllByProduct(product: Product)
}