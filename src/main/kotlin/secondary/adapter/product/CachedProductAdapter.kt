package secondary.adapter.product

import core.domain.product.model.Product
import core.domain.product.model.ProductId
import secondary.adapter.SecondaryAdapter
import secondary.repository.product.ProductCache
import secondary.repository.product.document.CachedProductMongoRepository

/**
 * Adapter for a [ProductAdapter], which stores the result in a [CachedProductMongoRepository]
 */
@SecondaryAdapter
class CachedProductAdapter(
    productApiService: ProductApiService,
    private val productRepository: ProductCache,
) : ProductAdapter(productApiService) {

    override fun fetchProduct(productId: ProductId): Product {
        return productRepository.getAndUpdateIfInvalid(productId, fallback = {
            super.fetchProduct(productId)
        })
    }


}
