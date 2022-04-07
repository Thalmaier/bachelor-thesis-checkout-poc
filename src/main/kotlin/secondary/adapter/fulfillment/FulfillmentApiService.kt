package secondary.adapter.fulfillment

import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.OutletId
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the fulfillment api
 */
class FulfillmentApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Get all available fulfillment for an [OutletId].
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun fetchAvailableFulfillment(outletId: OutletId): List<FulfillmentType> {
        logger.info { "Call to external system to fetch available fulfillment data for outlet $outletId" }
        MockTimeoutService.timeout(70, "fetchAvailableFulfillment")
        return when {
            outletId.startsWith("A") -> listOf(FulfillmentType.DELIVERY)
            else -> listOf(FulfillmentType.DELIVERY, FulfillmentType.PICKUP)
        }
    }

}
