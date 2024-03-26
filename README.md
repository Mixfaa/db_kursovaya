Курсовая кароче по базам данных
юзаю виртуальные потоки, так что без корутин, да и в принцепе вроде пока не нужны

Models:
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
 * parentCategory: Category (can be null)
 * subcategories: List of Category 
 * requiredProps: List of string
#### Register Request
 * name: String
 * requiredProps: List of string
 * subcategories: List of categories ids (can be null)
 * parentCategory: category id (can  be null)
---
### Product
 * id: (id) String
 * caption: String
 * categories: List of category
 * characteristics: Map String to String (prop to value)
 * description: String
 * price: Double
 * rate: Double
 * ordersCount: Long
#### Register request
 * caption: String
 * categories: List of categories ids
 * characteristics: Map String to String (prop to value)
 * description: String
 * price: Double
---
### Order
 * id: (id) String
 * products: List of Realized Products
 * owner: Account
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
 * owner: Account
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
 * owner: Account
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