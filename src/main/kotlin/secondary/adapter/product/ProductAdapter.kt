package secondary.adapter.product

import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import secondary.adapter.SecondaryAdapter
import secondary.adapter.exception.FetchingExternalResourceError

/**
 * Adapter for a [ProductPort]
 */
@SecondaryAdapter
open class ProductAdapter(
    private val productApiService: ProductApiService,
) : ProductPort {

    override fun fetchProduct(productId: ProductId): Product {
        return productApiService.fetchProduct(productId)
            ?.toProduct()
            ?: throw FetchingExternalResourceError("Could not fetch product $productId")
    }
}
