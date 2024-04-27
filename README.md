Курсовая кароче по базам данных
юзаю виртуальные потоки, так что без корутин, да и в принцепе вроде пока не нужны

# Models:

### Account (public)

* username: (id) String
* firstname: String
* lastname: String
* role: String (only ADMIN or CUSTOMER)

### Account (private) (when you request your account)

* username: (id) String
* firstname: String
* lastname: String
* email: String
* role: String (only ADMIN or CUSTOMER)
* shippingAddresses: List of String

#### Register Request

* username: String
* firstname: String
* lastname: String
* password: String
* role: String
* mailCode: String
* adminSecret: String (can be null) when you want to register as admin

---

### Category

* name: (id) String
* parentCategory: String (category id)
* subcategories: List of Categories ids
* requiredProps: List of string

#### Register Request

* name: String
* requiredProps: List of string
* subcategories: List of categories ids (can be null)
* parentCategory: category id (can be null)

---

### Product

* id: (id) String
* caption: String
* categories: List of categories ids
* characteristics: Map String to String (prop to value)
* description: String
* price: Double
* actualPrice: Double // price with discounts
* rate: Double
* ordersCount: Long
* images: List of strings

#### Register request

* caption: String
* categories: List of categories ids
* characteristics: Map String to String (prop to value)
* description: String
* price: Double
* images: List of strings

---

### Order

* id: (id) String
* products: List of Realized Products
* ownerId: Account`s id
* status: String (one of UNPAID,PROCESSING,SHIPPING,COMPLETED,CANCELED)
* shippingAddress: String
* timestamp: Date

#### Register request:

* products: List of products ids
* shippingAddress: String
* promoCode: String (can be null)

---

### Realized Product

* caption: String
* description: String
* productId: String
* price: Double

---

### Comment

* id: (id) String
* ownerId: Account`s id
* product: Product
* content: String
* rate: Double
* timestamp: Date

#### Register Request

* productId: String
* content: String
* rate: Double (0-5)

---

### Favourite list

* id: (id) String
* name: String
* ownerId: Account`s id
* products: List of product
* isPublic: Boolean

#### Register Request

* name: String
* isPublic: Boolean
* productsIds: List of products ids (can be null)

---

## Discounts

### DiscountByCategory

* id: (id) String
* description: String
* discount: Double (in %)
* targetCategories: List of categories ids

#### Register Request

* description: String
* discount: Double
* targetCategoriesIds: List of categories ids

### DiscountByProduct

* id: (id) String
* description: String
* discount: Double (in %)
* targetProducts: List of products

#### Register Request

* description: String
* discount: Double
* targetProductsIds List of products ids

### PromoCode

* id: (id) String
* description: String
* discount: Double (in %)
* code: String

#### Register Request

* description: String
* discount: Double
* code: String

# Endpoints

### account service

|                Url                 | Method |                                Accepts                                 |         Returns         |                                    Comment                                    |
|:----------------------------------:|:------:|:----------------------------------------------------------------------:|:-----------------------:|:-----------------------------------------------------------------------------:|
|             /register              |  post  |                        Account.RegisterRequest                         |         Account         |
|       /add_shipping_address        |  post  |                        shippingAddress: String                         |         Account         |
|      /remove_shipping_address      |  post  |                        shippingAddress: String                         |         Account         |
|              /get_me               |  get   |                                                                        |         Account         |
|            /find_users             |  get   |                  query:String, page:Int, pageSize:Int                  |         Account         |
|          /send_email_code          |  post  |                              email:String                              |                         |     send email with code to user`s email to pass it with register request     |
|         /category/register         |  post  |                        Category.RegisterRequest                        |        Category         |
|           /category/find           |  get   |                  query:String, page:Int, pageSize:Int                  |   Page of Categories    |
|           /category/list           |  get   |                        page:Int, pageSize: Int                         |   Page of categories    |
|          / category/count          |  get   |                                                                        |           Int           |
|         /product/register          |  post  |                        Product.RegisterRequest                         |         Product         |
|          /product/delete           |  post  |                           productId: String                            |                         |
|           /product/find            |  get   |                  query:String, page:Int, pageSize:Int                  |    Page of products     |
|           /product/count           |  get   |                                                                        |           Int           |
|          /product/findV2           |  get   | query: QueryConstructor, sort: SortConstructor, page:Int, pageSize:Int |    Page of products     |
|          /product/findV3           |  get   |     query: QueryConstructor, sort: String, page:Int, pageSize:Int      |    Page of products     | Sort must be one of PRICE_DESCENDING, PRICE_ASCENDING, ORDER_COUNT_DESCENDING |
|           /product/edit            |  post  |                                Product                                 |         Product         |                                                                               |                 
|         /discount/register         |  post  |                        Discount.RegisterRequest                        |        Discount         |
|          /discount/delete          |  post  |                           discountId:String                            |                         |
|           /discount/list           |  get   |                         page:Int, pageSize:Int                         |    Page of discounts    |
|          /order/register           |  post  |                         Order.RegisterRequest                          |          Order          |
|           /order/cancel            |  post  |                             orderId:String                             |          Order          |
|        /order/change_status        |  post  |                   orderId:String, newStatus: Status                    |          Order          |
|           /order/list_my           |  get   |                         page:Int, pageSize:Int                         |     Page of orders      |
|          /order/count_my           |  get   |                                                                        |           Int           |
|          /order/calculate          |  get   |                         Order.RegisterRequest                          | List of RealizedProduct |                      calculates order with all discounts                      |
|         /comments/register         |  post  |                        Comment.RegisterRequest                         |         Comment         |
|          /comments/delete          |  post  |                           commentId: String                            |                         |
|           /comments/list           |  get   |               productId:String, page:Int, pageSize: Int                |    Page of comments     |
|          /favlist/create           |  post  |                     FavouriteList.RegisterRequest                      |      FavouriteList      |
|          /favlist/delete           |  post  |                             listId:String                              |                         |
|     /favlist/change_visibility     |  post  |                     listId:String, isPublic:String                     |      FavouriteList      |
|        /favlist/add_product        |  post  |                    listId:String, productId:String                     |      FavouriteList      |
|      /favlist/remove_product       |  post  |                    listId:String, productId:String                     |      FavouriteList      |
|          /favlist/get_my           |  get   |                                                                        |  List of FavouteLists   |
|        /favlist/get_public         |  get   |                             listId:String                              |      FavouriteList      |                   get favourite list by id if it is public                    |
| /favlist/find_account_public_lists |  get   |                            accountId:String                            | List of favourite lists |                          get user`s favourite lists                           |
