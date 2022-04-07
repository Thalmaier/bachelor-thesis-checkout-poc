package core.application.checkoutdata

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.*
import core.domain.basketdata.model.customer.Customer
import core.domain.common.Port
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.payment.model.Payment

@Port
interface CheckoutDataApiPort {
    /**
     * Sets the [Customer] data for an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setCustomer(basketId: BasketId, customer: Customer): Aggregates

    /**
     * Sets the [ShippingAddress][Address] data for an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN].
     * This recalculates the shipping cost of the [BasketData]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setShippingAddress(basketId: BasketId, shippingAddress: Address): Aggregates

    /**
     * Sets the [ShippingAddress][Address] data for an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setBillingAddress(basketId: BasketId, billingAddress: Address): Aggregates

    /**
     * Sets the [FulfillmentType] data for an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN].
     * This recalculates the shipping cost of the [BasketData]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setFulfillment(basketId: BasketId, fulfillmentType: FulfillmentType): Aggregates

    /**
     * Sets the checkout data if not null for an existing [BasketData]. The [BasketData] needs to be in status [BasketStatus.OPEN].
     * This can result in a recalculation of the [BasketData]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setCheckoutData(
        basketId: BasketId,
        fulfillment: FulfillmentType?,
        shippingAddress: Address?,
        billingAddress: Address?,
        customer: Customer?,
        payment: Payment?,
    ): Aggregates

    fun findCheckoutDataById(basketId: BasketId): Aggregates
}
