package com.mixfa.marketplace.marketplace.service.repo

import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.marketplace.model.FavouriteList
import com.mixfa.marketplace.marketplace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface FavoriteListRepository : MongoRepository<FavouriteList, String> {
    fun findAllByOwnerAndIsPublicTrue(owner: Account): List<FavouriteList>
    fun findByIdAndIsPublicTrue(id: String): FavouriteList?
    fun findAllByOwner(owner: Account): List<FavouriteList>
    fun countByOwner(owner: Account): Int
    fun deleteByIdAndOwner(id: String, owner: Account)
    fun findAllByProductsContains(product: Product, pageable: Pageable) : Page<FavouriteList>
}