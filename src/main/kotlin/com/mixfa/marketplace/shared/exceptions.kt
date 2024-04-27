package com.mixfa.marketplace.shared

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.ExcifyOptionalOrThrow
import com.mixfa.excify.FastThrowable
import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount

class NotFoundException(subject: String) : FastThrowable("$subject not found") {
    companion object
}

@ExcifyCachedException
class LargePageSizeException : FastThrowable("Page size is too big, should be <= $MAX_PAGE_SIZE") {
    companion object
}

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
)

@ExcifyCachedException
class ProductAlreadyInListException : FastThrowable("Product already in list") {
    companion object
}

@ExcifyCachedException
class ProductNotInListException : FastThrowable("Product not in list") {
    companion object
}

@ExcifyCachedException
class FavouriteListsLimitException : FastThrowable("Favourite lists per user limit") {
    companion object
}

@ExcifyCachedException
class FavouriteListProductsLimitException : FastThrowable("Products per list limit") {
    companion object
}

@ExcifyCachedException
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

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = AbstractDiscount::class, methodName = "orThrow")
val discountNotFound = NotFoundException("Discount")

data class ErrorModel(val message: String) {
    constructor(ex: Throwable) : this(ex.message ?: ex.localizedMessage ?: "Unresolved error")
}