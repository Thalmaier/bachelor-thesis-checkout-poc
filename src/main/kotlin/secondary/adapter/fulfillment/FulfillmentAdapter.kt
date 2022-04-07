package secondary.adapter.fulfillment

import core.domain.basket.FulfillmentPort
import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.OutletId
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [FulfillmentPort]
 */
@SecondaryAdapter
class FulfillmentAdapter(private val fulfillmentApiService: FulfillmentApiService) : FulfillmentPort {

    override fun getPossibleFulfillment(outletId: OutletId): List<FulfillmentType> {
        return fulfillmentApiService.fetchAvailableFulfillment(outletId)
    }

}
