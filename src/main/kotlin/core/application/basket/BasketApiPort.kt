package core.application.basket

import core.domain.basket.model.*
import core.domain.basket.model.customer.Customer
import core.domain.common.Port
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.payment.model.Payment

/**
 * Port for handling all [Basket] api request
 */
@Port
interface BasketApiPort {

    /**
     * Finds a [Basket] by a [BasketId]
     * @return the corresponding basket
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     */
    fun findBasketById(basketId: BasketId): Basket

    /**
     * Creates a new [Basket] for a [OutletId]
     * @param customer Optionally the customer data can be set during the creation
     * @return a new [Basket] ressource
     */
    fun createBasket(outletId: OutletId, customer: Customer?): Basket

    /**
     * Cancels an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun cancelBasket(basketId: BasketId): Basket

    /**
     * Returns a list of all available [FulfillmentType]s for an existing [Basket].
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun getAvailableFulfillment(basketId: BasketId): List<FulfillmentType>

    /**
     * Sets the [Customer] data for an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setCustomer(basketId: BasketId, customer: Customer): Basket

    /**
     * Sets the [FulfillmentType] data for an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN].
     * This recalculates the shipping cost of the [Basket]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setFulfillment(basketId: BasketId, fulfillmentType: FulfillmentType): Basket

    /**
     * Sets the [ShippingAddress][Address] data for an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN].
     * This recalculates the shipping cost of the [Basket]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setShippingAddress(basketId: BasketId, shippingAddress: Address): Basket

    /**
     * Sets the [ShippingAddress][Address] data for an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setBillingAddress(basketId: BasketId, billingAddress: Address): Basket

    /**
     * Sets the checkout data if not null for an existing [Basket]. The [Basket] needs to be in status [BasketStatus.OPEN].
     * This can result in a recalculation of the [Basket]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setCheckoutData(
        basketId: BasketId, fulfillment: FulfillmentType?, shippingAddress: Address?,
        billingAddress: Address?, customer: Customer?, payment: Payment?,
    ): Basket
}
