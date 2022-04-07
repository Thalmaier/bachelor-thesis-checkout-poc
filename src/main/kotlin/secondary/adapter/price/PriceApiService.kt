package secondary.adapter.price

import core.domain.price.generateRandomMonetaryAmount
import core.domain.price.model.PriceId
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the pricing api
 */
class PriceApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Returns a [PriceApiService] for an [PriceId]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun fetchPrice(priceId: PriceId): PriceApiResponse? {
        logger.info { "Fetch price for $priceId from the pricing system" }
        MockTimeoutService.timeout(30, "fetchPrice")
        return PriceApiResponse(
            id = priceId,
            grossAmount = generateRandomMonetaryAmount()
        )
    }

}
