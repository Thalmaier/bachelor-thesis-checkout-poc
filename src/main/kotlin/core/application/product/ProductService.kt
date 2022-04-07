package core.application.product

import core.domain.product.model.Product
import core.domain.product.model.ProductId

/**
 * Service for handling all [Product] related tasks
 */
interface ProductService {

    /**
     * Returns the [Product] for a [ProductId].
     * The external product data receives updates over time, so calling this method with the same [ProductId],
     * can yield in a different [Product].
     */
    fun fetchProductInformation(productId: ProductId): Product

}
