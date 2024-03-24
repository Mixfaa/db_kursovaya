package com.mixfa.marketplace.account.service

import com.mixfa.marketplace.account.model.Account
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRepository : MongoRepository<Account, String> {
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun findByUsername(username: String): Optional<Account>

    @Query("{ \$text :  {\$search: \"?1\"}}")
    fun findAllByText(query: String, pageable: Pageable): Page<Account>
}