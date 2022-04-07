package primary.basket

import core.domain.basket.model.Address
import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.customer.Customer
import core.domain.payment.model.Payment

/**
 * Data class for deserialization of the request body for setting all checkout data
 */
data class SetCheckoutDataApiRequest(
    val fulfillment: FulfillmentType?,
    val customer: Customer?,
    val shippingAddress: Address?,
    val billingAddress: Address?,
    val payment: Payment?,
)
