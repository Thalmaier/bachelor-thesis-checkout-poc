package core.application.pricing

import core.application.ApplicationService
import core.domain.basketdata.model.OutletId
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import core.domain.product.model.ProductId
import mu.KotlinLogging

/**
 * Implementation of the [PricingService]
 */
@ApplicationService
class PricingApplicationService(
    private val pricePort: PricePort,
) : PricingService {

    private val logger = KotlinLogging.logger {}

    override fun fetchPriceInformation(outletId: OutletId, productId: ProductId): Price {
        logger.info { "Fetch price information for outlet $outletId and product $productId" }
        return pricePort.fetchPrice(PriceId(outletId, productId))
    }

}
