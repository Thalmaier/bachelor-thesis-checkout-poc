package core.domain.basketdata

import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.OutletId
import core.domain.common.Port

/**
 * Port to the external fulfillment api
 */
@Port
interface FulfillmentPort {

    /**
     * Returns a list of all possible [FulfillmentType] for a [OutletId]
     */
    fun getPossibleFulfillment(outletId: OutletId): List<FulfillmentType>

}
