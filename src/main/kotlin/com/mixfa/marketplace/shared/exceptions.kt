package com.mixfa.marketplace.shared

import ExcifyCachedException
import ExcifyException
import ExcifyOptionalOrThrow
import FastThrowable
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.marketplace.model.*

@ExcifyException
class NotFoundException(subject: String) : FastThrowable("$subject not found") {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class LargePageSizeException() : FastThrowable("Page size is too big, should be <= $MAX_PAGE_SIZE") {
    companion object
}

@ExcifyException
class ProductCharacteristicsNotSetException(
    requiredCharacteristics: Collection<String>,
    providedCharacteristics: Collection<String>
) : FastThrowable(
    """Can`t register product, required characteristics not set
    Required characteristics:
    $requiredCharacteristics
    
    Provided characteristics:
    $providedCharacteristics
""".trimMargin()
) {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class ProductAlreadyInListException : FastThrowable("Product already in list") {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class ProductNotInListException : FastThrowable("Product not in list") {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class FavouriteListsLimitException : FastThrowable("One user can`t create more than 10 favourite lists") {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class FavouriteListProductsLimitException : FastThrowable("One list can`t contain more than 50 products") {
    companion object
}

@ExcifyException(cacheNoArgs = true)
class FastAccessException : FastThrowable("Access denied") {
    companion object
}

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = FavouriteList::class, methodName = "orThrow")
val favouriteListNotFound = NotFoundException("Favourite list")

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = Comment::class, methodName = "orThrow")
val commentNotFound = NotFoundException("Comment")

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = Category::class, methodName = "orThrow")
val categoryNotFound = NotFoundException("Category")

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = Product::class, methodName = "orThrow")
val productNotFound = NotFoundException("Product")

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = Order::class, methodName = "orThrow")
val orderNotFound = NotFoundException("Order")

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = Account::class, methodName = "orThrow")
val accountNotFound = NotFoundException("Account")

data class ErrorModel(val message: String) {
    constructor(ex: Exception) : this(ex.message ?: ex.localizedMessage ?: "Exception without message")
}