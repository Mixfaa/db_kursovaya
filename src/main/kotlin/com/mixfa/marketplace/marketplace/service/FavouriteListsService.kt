package com.mixfa.marketplace.marketplace.service

import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.marketplace.model.FavouriteList
import com.mixfa.marketplace.marketplace.model.Product
import com.mixfa.marketplace.marketplace.service.repo.FavoriteListRepository
import com.mixfa.marketplace.shared.*
import org.springframework.context.ApplicationListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class FavouriteListsService(
    private val accountService: AccountService,
    private val productService: ProductService,
    private val favoriteListRepo: FavoriteListRepository
) : ApplicationListener<ProductService.Event> {

    @PreAuthorize("hasAuthority('FAVLIST:READ')")
    private fun findFavouriteListSecure(listId: String): FavouriteList {
        val favouriteList = favoriteListRepo.findById(listId).orThrow()
        SecurityUtils.getAuthenticatedPrincipal().throwIfNot(favouriteList.owner)
        return favouriteList
    }

    @PreAuthorize("hasAuthority('FAVLIST:WRITE')")
    fun createList(request: FavouriteList.RegisterRequest): FavouriteList {
        val account = accountService.getAuthenticatedAccount().orThrow()

        if (favoriteListRepo.countByOwner(account) >= MAX_LISTS_PER_USER)
            throw FavouriteListsLimitException.get()

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    private fun handleProductDeletion(product: Product) {
        iteratePages(bindToFetchFun(favoriteListRepo::findAllByProductsContains, product)) { favoriteList ->
            favoriteListRepo.save(favoriteList.copy(
                products = favoriteList.products - product
            ))
        }
    }

    @PreAuthorize("hasAuthority('FAVLIST:WRITE')")
    fun deleteList(listId: String) {
        val account = accountService.getAuthenticatedAccount().orThrow()
        favoriteListRepo.deleteByIdAndOwner(listId, account)
    }

    @PreAuthorize("hasAuthority('FAVLIST:WRITE')")
    fun changeListVisibility(listId: String, isPublic: Boolean) {
        val favouriteList = findFavouriteListSecure(listId)

        if (favouriteList.isPublic == isPublic) return

        favoriteListRepo.save(
            favouriteList.copy(isPublic = isPublic)
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:WRITE')")
    fun addProductToList(listId: String, productId: String): FavouriteList {
        val favouriteList = findFavouriteListSecure(listId)

        if (favouriteList.products.size >= MAX_PRODUCTS_PER_LIST)
            throw FavouriteListsLimitException.get()

        val product = productService.findProductById(productId).orThrow()

        if (favouriteList.products.contains(product))
            throw ProductAlreadyInListException.get()

        return favoriteListRepo.save(
            favouriteList.copy(
                products = favouriteList.products + product
            )
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:WRITE')")
    fun removeProductFromList(listId: String, productId: String): FavouriteList {
        val favouriteList = findFavouriteListSecure(listId)

        val product = productService.findProductById(productId).orThrow()

        if (!favouriteList.products.contains(product))
            throw ProductNotInListException.get()

        return favoriteListRepo.save(
            favouriteList.copy(
                products = favouriteList.products - product
            )
        )
    }

    @PreAuthorize("hasAuthority('FAVLIST:READ')")
    fun getMyFavouriteLists(): List<FavouriteList> {
        val account = accountService.getAuthenticatedAccount().orThrow()
        return favoriteListRepo.findAllByOwner(account)
    }

    @PreAuthorize("hasAuthority('FAVLIST:READ')")
    fun getPublicFavouriteList(listId: String): FavouriteList {
        return favoriteListRepo.findByIdAndIsPublicTrue(listId).orThrow()
    }

    @PreAuthorize("hasAuthority('FAVLIST:READ')")
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