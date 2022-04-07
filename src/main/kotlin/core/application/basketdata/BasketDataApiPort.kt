package core.application.basketdata

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.*
import core.domain.basketdata.model.customer.Customer
import core.domain.common.Port
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError

/**
 * Port for handling all basket api request
 */
@Port
interface BasketDataApiPort {

    /**
     * Returns a [BasketData] corresponding to the passed [BasketId]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     */
    fun findBasketDataById(basketId: BasketId): Aggregates

    /**
     * Creates a new [Aggregates] for a [OutletId]
     * @param customer Optionally the customer data can be set during the creation
     */
    fun createBasket(outletId: OutletId, customer: Customer?): Aggregates

    /**
     * Cancels an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun cancelBasket(basketId: BasketId): Aggregates

    /**
     * Returns a list of all available [FulfillmentType]s for an existing [BasketData].
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun getAvailableFulfillment(basketId: BasketId): List<FulfillmentType>

}
