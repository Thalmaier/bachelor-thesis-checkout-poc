package secondary.adapter.product

import core.domain.product.model.ProductId
import core.domain.product.model.Vat
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService
import java.math.BigDecimal
import kotlin.random.Random

/**
 * Calls used to make api request to the product api
 */
class ProductApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Returns a [ProductApiResponse] for an [ProductId]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun fetchProduct(productId: ProductId): ProductApiResponse? {
        logger.info { "Fetch product for $productId from the product system" }
        MockTimeoutService.timeout(40, "fetchProduct")
        return ProductApiResponse(
            id = productId,
            name = "TestProduct${Random.nextInt(0, 100000)}",
            vat = Vat(
                Random.nextInt(0, 5),
                BigDecimal(19.0)
            )
        )
    }

}
