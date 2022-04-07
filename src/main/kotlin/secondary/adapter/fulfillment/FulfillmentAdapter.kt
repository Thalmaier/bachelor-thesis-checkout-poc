package secondary.adapter.fulfillment

import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.OutletId
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [FulfillmentPort]
 */
@SecondaryAdapter
class FulfillmentAdapter(
    private val fulfillmentApiService: FulfillmentApiService,
) : FulfillmentPort {

    override fun getPossibleFulfillment(outletId: OutletId): List<FulfillmentType> {
        return fulfillmentApiService.fetchAvailableFulfillment(outletId)
    }

}
