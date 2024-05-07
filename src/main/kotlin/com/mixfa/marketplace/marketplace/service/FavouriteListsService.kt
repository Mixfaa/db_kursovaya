package com.mixfa.marketplace.marketplace.service

import arrow.core.partially1
import com.mixfa.`excify-either`.makeMemorizedException
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.FavouriteList
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.FavoriteListRepository
import com.mixfa.marketplace.shared.*
import jakarta.validation.Valid
import kotlinx.coroutines.GlobalScope
import org.springframework.context.ApplicationListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class FavouriteListsService(
    private val accountService: AccountService,
    private val productService: ProductService,
    private val favoriteListRepo: FavoriteListRepository
) : ApplicationListener<ProductService.Event> {

    private fun findFavListByAuthenticated(listId: String): FavouriteList {
        val favouriteList = favoriteListRepo.findById(listId).orThrow()
        authenticatedPrincipal().throwIfNot(favouriteList.owner)
        return favouriteList
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun createList(@Valid request: FavouriteList.RegisterRequest): FavouriteList {
        val account = accountService.getAuthenticatedAccount().orThrow()

        if (favoriteListRepo.countByOwner(account) >= MAX_LISTS_PER_USER)
            throw makeMemorizedException("Favourite lists per user reached")

        val products = buildList {
            if (request.productsIds != null)
                addAll(productService.findProductsByIdsOrThrow(request.productsIds))
        }

        return favoriteListRepo.save(
            FavouriteList(
                name = request.name,
                owner = account,
                isPublic = request.isPublic,
                products = products
            )
        )
    }

    private fun handleProductDeletion(product: Product) {
        GlobalScope.launchIO {
            iteratePages(favoriteListRepo::findAllByProductsContains.partially1(product)) { favoriteList ->
                favoriteListRepo.save(
                    favoriteList.copy(
                        products = favoriteList.products - product
                    )
                )
            }
        }
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun deleteList(listId: String) {
        val account = accountService.getAuthenticatedAccount().orThrow()
        favoriteListRepo.deleteByIdAndOwner(listId, account)
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun changeListVisibility(listId: String, isPublic: Boolean) {
        val favouriteList = findFavListByAuthenticated(listId)

        if (favouriteList.isPublic == isPublic) return

        favoriteListRepo.save(
            favouriteList.copy(isPublic = isPublic)
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun addProductToList(listId: String, productId: String): FavouriteList {
        val favouriteList = findFavListByAuthenticated(listId)

        if (favouriteList.products.size >= MAX_PRODUCTS_PER_LIST)
            throw makeMemorizedException("Products limit per list reached")

        val product = productService.findProductById(productId).orThrow()

        if (favouriteList.products.contains(product))
            throw makeMemorizedException("Product already in list")

        return favoriteListRepo.save(
            favouriteList.copy(
                products = favouriteList.products + product
            )
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun removeProductFromList(listId: String, productId: String): FavouriteList {
        val favouriteList = findFavListByAuthenticated(listId)

        val product = productService.findProductById(productId).orThrow()

        if (!favouriteList.products.contains(product))
            throw makeMemorizedException("Product not found in list")

        return favoriteListRepo.save(
            favouriteList.copy(
                products = favouriteList.products - product
            )
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:EDIT')")
    fun getMyFavouriteLists(): List<FavouriteList> {
        val account = accountService.getAuthenticatedAccount().orThrow()
        return favoriteListRepo.findAllByOwner(account)
    }

    fun getPublicFavouriteList(listId: String): FavouriteList {
        return favoriteListRepo.findByIdAndIsPublicTrue(listId).orThrow()
    }

    fun findPublicFavouriteListsOf(accountId: String): List<FavouriteList> {
        val account = accountService.findAccount(accountId).orThrow()
        return favoriteListRepo.findAllByOwnerAndIsPublicTrue(account)
    }

    override fun onApplicationEvent(event: ProductService.Event) = when (event) {
        is ProductService.Event.ProductDelete -> handleProductDeletion(event.product)
        is ProductService.Event.ProductRegister -> {}
    }

    companion object {
        const val MAX_LISTS_PER_USER = 10
        const val MAX_PRODUCTS_PER_LIST = 50
    }
}