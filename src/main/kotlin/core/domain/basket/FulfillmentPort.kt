package core.domain.basket

import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.OutletId
import core.domain.common.Port

/**
 * Port to the external fulfillment api
 */
@Port
interface FulfillmentPort {

    /**
     * Returns a list of all possible [FulfillmentType]s
     */
    fun getPossibleFulfillment(outletId: OutletId): List<FulfillmentType>

}
