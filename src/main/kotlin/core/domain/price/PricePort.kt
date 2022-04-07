package core.domain.price

import core.domain.common.Port
import core.domain.price.model.Price
import core.domain.price.model.PriceId

/**
 * Port used for communication with the [Price] adapter
 */
@Port
interface PricePort {

    /**
     * Fetches the price for a certain [PriceId]
     * The result can be different depending on the point in time it was called.
     */
    fun fetchPrice(priceId: PriceId): Price

}
