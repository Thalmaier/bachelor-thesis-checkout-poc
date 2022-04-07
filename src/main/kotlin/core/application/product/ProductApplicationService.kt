package core.application.product

import core.application.ApplicationService
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import mu.KotlinLogging

/**
 * Implementation of the [ProductService]
 */
@ApplicationService
class ProductApplicationService(private val productPort: ProductPort) : ProductService {

    private val logger = KotlinLogging.logger {}

    override fun fetchProductInformation(productId: ProductId): Product {
        logger.info { "Fetch product information for id $productId" }
        return productPort.fetchProduct(productId)
    }

}
