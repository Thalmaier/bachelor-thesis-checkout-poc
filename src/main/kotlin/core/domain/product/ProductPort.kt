package core.domain.product

import core.domain.common.Port
import core.domain.product.model.Product
import core.domain.product.model.ProductId

/**
 * Port used for communication with the [Product] adapter
 */
@Port
interface ProductPort {

    /**
     * Fetches the product data for a certain [Product]
     * The result can be different depending on the point in time it was called.
     */
    fun fetchProduct(productId: ProductId): Product

}
