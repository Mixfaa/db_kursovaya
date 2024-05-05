package com.mixfa.marketplace.controller

import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import com.mixfa.marketplace.marketplace.service.*
import com.mixfa.marketplace.shared.model.AssembleableSortConstructor
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.model.PrecompiledSort
import com.mixfa.marketplace.shared.model.QueryConstructor
import com.mixfa.marketplace.shared.orThrow
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v2/marketplace")
class MarketplaceControllerV2(
    private val categoryService: CategoryService,
    private val commentService: CommentService,
    private val discountService: DiscountService,
    private val orderService: OrderService,
    private val productService: ProductService,
    private val favouriteListsService: FavouriteListsService
) {
    /*
    * categories
    */

    @PostMapping("/categories/register")
    fun registerCategory(@RequestBody request: Category.RegisterRequest) =
        categoryService.registerCategory(request)

    @GetMapping("/categories/find")
    fun findCategories(query: String, page: Int, pageSize: Int) =
        categoryService.findCategories(query, CheckedPageable(page, pageSize))

    @GetMapping("/categories")
    fun listCategories(page: Int, pageSize: Int) =
        categoryService.listCategories(CheckedPageable(page, pageSize))

    @GetMapping("/categories/count")
    fun countCategories() = categoryService.countCategories()

    /*
    * Products
    */

    @PostMapping("/products/register")
    fun registerProduct(@RequestBody request: Product.RegisterRequest) =
        productService.registerProduct(request)

    @DeleteMapping("/products/{productId}")
    fun deleteProduct(@PathVariable productId: String) =
        productService.deleteProduct(productId)

    @GetMapping("/products/{productId}")
    fun getProduct(@PathVariable productId: String) = productService.findProductById(productId).orThrow()

    @GetMapping("/products/find")
    fun findProducts(query: String, page: Int, pageSize: Int) =
        productService.findProducts(query, CheckedPageable(page, pageSize))

    @GetMapping("/products/count")
    fun countProducts() =
        productService.countProducts()

    @GetMapping("/products/findV2")
    fun findProductsV2(query: QueryConstructor, sort: AssembleableSortConstructor, page: Int, pageSize: Int) =
        productService.findProducts(query, sort, CheckedPageable(page, pageSize))

    @GetMapping("/products/findV3")
    fun findProductsV3(query: QueryConstructor, sort: PrecompiledSort, page: Int, pageSize: Int) =
        productService.findProducts(query, sort, CheckedPageable(page, pageSize))

    @PostMapping("/products/{productId}/image")
    fun addProductImage(@PathVariable productId: String, image: String) =
        productService.addProductImage(productId, image)

    @DeleteMapping("/products/{productId}/image")
    fun deleteProductImage(@PathVariable productId: String, image: String) =
        productService.removeProductImage(productId, image)
    /*
    * Discounts
    */

    @PostMapping("/discounts/register")
    fun registerDiscount(@RequestBody request: AbstractDiscount.AbstractRegisterRequest) =
        discountService.registerDiscount(request)

    @DeleteMapping("/discounts/{discountId}")
    fun deleteDiscount(@PathVariable discountId: String) =
        discountService.deleteDiscount(discountId)

    @GetMapping("/discounts")
    fun listDiscounts(page: Int, pageSize: Int) =
        discountService.listDiscounts(CheckedPageable(page, pageSize))

    @GetMapping("/discounts/{query}")
    fun findDiscounts(@PathVariable query: String, page: Int, pageSize: Int) =
        discountService.findDiscounts(query, CheckedPageable(page, pageSize))

    /*
    * Orders
    */

    @PostMapping("/orders/register")
    fun registerOrder(@RequestBody request: Order.RegisterRequest) =
        orderService.registerOrder(request)

    @PostMapping("/orders/{orderId}/cancel")
    fun cancelOrder(@PathVariable orderId: String) =
        orderService.cancelOrder(orderId)

    @PostMapping("/orders/{orderId}/change_status")
    fun changeOrderStatus(@PathVariable orderId: String, newStatus: OrderStatus) =
        orderService.changeOrderStatus(orderId, newStatus)

    @GetMapping("/orders/list_my")
    fun listMyOrders(page: Int, pageSize: Int) =
        orderService.listMyOrders(CheckedPageable(page, pageSize))

    @GetMapping("/orders/count_my")
    fun countMyOrders() =
        orderService.countMyOrders()

    @GetMapping("/orders/calculate")
    fun calculateOrder(request: Order.RegisterRequest) =
        orderService.calculateOrderCost(request)

    /*
     * Comments
     */

    @PostMapping("/comments/register")
    fun registerComment(@RequestBody request: Comment.RegisterRequest) =
        commentService.registerComment(request)

    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(@PathVariable commentId: String) =
        commentService.deleteComment(commentId)

    @GetMapping("/comments/from_product/{productId}")
    fun listComments(@PathVariable productId: String, page: Int, pageSize: Int) =
        commentService.listProductComments(productId, CheckedPageable(page, pageSize))

    /*
     * Favourite
     */

    @PostMapping("/favlists/create")
    fun createFavouriteList(@RequestBody request: FavouriteList.RegisterRequest) =
        favouriteListsService.createList(request)

    @DeleteMapping("/favlists/{listId}")
    fun deleteFavouriteList(@PathVariable listId: String) =
        favouriteListsService.deleteList(listId)

    @PostMapping("/favlists/{listId}/change_visibility")
    fun changeFavouriteListVisibility(@PathVariable listId: String, isPublic: Boolean) =
        favouriteListsService.changeListVisibility(listId, isPublic)

    @PostMapping("/favlists/{listId}/product/{productId}")
    fun addProductToFavouriteList(@PathVariable listId: String, @PathVariable productId: String) =
        favouriteListsService.addProductToList(listId, productId)

    @DeleteMapping("/favlists/{listId}/product/{productId}")
    fun removeProductFromFavouriteList(@PathVariable listId: String, @PathVariable productId: String) =
        favouriteListsService.removeProductFromList(listId, productId)

    @GetMapping("/favlists/my")
    fun getMyFavouriteLists() =
        favouriteListsService.getMyFavouriteLists()

    @GetMapping("/favlists/{listId}/get_public")
    fun getPublicFavouriteList(@PathVariable listId: String) =
        favouriteListsService.getPublicFavouriteList(listId)

    @GetMapping("/favlists/from_account/{accountId}")
    fun findPublicFavouriteListsOf(@PathVariable accountId: String) =
        favouriteListsService.findPublicFavouriteListsOf(accountId)
}