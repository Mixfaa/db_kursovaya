package com.mixfa.marketplace.controller

import com.mixfa.marketplace.marketplace.model.*
import com.mixfa.marketplace.marketplace.model.discount.AbstractDiscount
import com.mixfa.marketplace.marketplace.service.*
import com.mixfa.marketplace.shared.model.CheckedPageable
import com.mixfa.marketplace.shared.model.PrecompiledSort
import com.mixfa.marketplace.shared.model.QueryConstructor
import com.mixfa.marketplace.shared.model.SortConstructor
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/marketplace")
class MarketplaceController(
    private val categoryService: CategoryService,
    private val commentService: CommentService,
    private val discountService: DiscountService,
    private val orderService: OrderService,
    private val productService: ProductService,
    private val favouriteListsService: FavouriteListsService
) {
    @GetMapping("/status")
    fun getStatus(): String = "Marketplace probably works"

    /*
    * categories
    */

    @PostMapping("/category/register")
    fun registerCategory(@RequestBody request: Category.RegisterRequest) =
        categoryService.registerCategory(request)

    @GetMapping("/category/find")
    fun findCategories(query: String, page: Int, pageSize: Int) =
        categoryService.findCategories(query, CheckedPageable(page, pageSize))

    @GetMapping("/category/list")
    fun listCategories(page: Int, pageSize: Int) =
        categoryService.listCategories(CheckedPageable(page, pageSize))

    @GetMapping("/category/count")
    fun countCategories() = categoryService.countCategories()

    /*
    * Products
    */

    @PostMapping("/product/register")
    fun registerProduct(@RequestBody request: Product.RegisterRequest) =
        productService.registerProduct(request)

    @PostMapping("/product/delete")
    fun deleteProduct(productId: String) =
        productService.deleteProduct(productId)

    @GetMapping("/product/find")
    fun findProducts(query: String, page: Int, pageSize: Int) =
        productService.findProducts(query, CheckedPageable(page, pageSize))

    @GetMapping("/product/count")
    fun countProducts() =
        productService.countProducts()

    @GetMapping("/product/findV2")
    fun findProductsV2(query: QueryConstructor, sort: SortConstructor, page: Int, pageSize: Int) =
        productService.findProducts(query, sort, CheckedPageable(page, pageSize))

    @GetMapping("/product/findV3")
    fun findProductsV3(query: QueryConstructor, sort: PrecompiledSort, page: Int, pageSize: Int) =
        productService.findProducts(query, sort, CheckedPageable(page, pageSize))

    @PostMapping("/product/edit")
    fun editProduct(@RequestBody product: Product) =
        productService.editProduct(product)

    /*
    * Discounts
    */

    @PostMapping("/discount/register")
    fun registerDiscount(@RequestBody request: AbstractDiscount.AbstractRegisterRequest) =
        discountService.registerDiscount(request)

    @PostMapping("/discount/delete")
    fun deleteDiscount(discountId: String) =
        discountService.deleteDiscount(discountId)

    @GetMapping("/discount/list")
    fun listDiscounts(page: Int, pageSize: Int) =
        discountService.listDiscounts(CheckedPageable(page, pageSize))

    /*
    * Orders
    */

    @PostMapping("/order/register")
    fun registerOrder(@RequestBody request: Order.RegisterRequest) =
        orderService.registerOrder(request)

    @PostMapping("/order/cancel")
    fun cancelOrder(orderId: String) =
        orderService.cancelOrder(orderId)

    @PostMapping("/order/change_status")
    fun changeOrderStatus(orderId: String, newStatus: OrderStatus) =
        orderService.changeOrderStatus(orderId, newStatus)

    @GetMapping("/order/list_my")
    fun listMyOrders(page: Int, pageSize: Int) =
        orderService.listMyOrders(CheckedPageable(page, pageSize))

    @GetMapping("/order/count_my")
    fun countMyOrders() =
        orderService.countMyOrders()

    @GetMapping("/order/calculate")
    fun calculateOrder(request: Order.RegisterRequest) =
        orderService.calculateOrderCost(request)

    /*
     * Comments
     */

    @PostMapping("/comments/register")
    fun registerComment(@RequestBody request: Comment.RegisterRequest) =
        commentService.registerComment(request)

    @PostMapping("/comment/delete")
    fun deleteComment(commentId: String) =
        commentService.deleteComment(commentId)

    @GetMapping("/comments/list")
    fun listComments(productId: String, page: Int, pageSize: Int) =
        commentService.listProductComments(productId, CheckedPageable(page, pageSize))

    /*
     * Favourite
     */

    @PostMapping("/favlist/create")
    fun createFavouriteList(@RequestBody request: FavouriteList.RegisterRequest) =
        favouriteListsService.createList(request)

    @PostMapping("/favlist/delete")
    fun deleteFavouriteList(listId: String) =
        favouriteListsService.deleteList(listId)

    @PostMapping("/favlist/change_visibility")
    fun changeFavouriteListVisibility(listId: String, isPublic: Boolean) =
        favouriteListsService.changeListVisibility(listId, isPublic)

    @PostMapping("/favlist/add_product")
    fun addProductToFavouriteList(listId: String, productId: String) =
        favouriteListsService.addProductToList(listId, productId)

    @PostMapping("/favlist/remove_product")
    fun removeProductFromFavouriteList(listId: String, productId: String) =
        favouriteListsService.removeProductFromList(listId, productId)

    @GetMapping("/favlist/get_my")
    fun getMyFavouriteLists() =
        favouriteListsService.getMyFavouriteLists()

    @GetMapping("/favlist/get_public")
    fun getPublicFavouriteList(listId: String) =
        favouriteListsService.getPublicFavouriteList(listId)

    @GetMapping("/favlist/find_account_public_lists")
    fun findPublicFavouriteListsOf(accountId: String) =
        favouriteListsService.findPublicFavouriteListsOf(accountId)
}