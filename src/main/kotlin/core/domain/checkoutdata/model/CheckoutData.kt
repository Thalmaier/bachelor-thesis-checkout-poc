package core.domain.checkoutdata.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.Customer
import core.domain.validation.model.ValidationResult

interface CheckoutData {

    @JsonIgnore fun getBasketId(): BasketId
    fun getCustomer(): Customer?
    fun getFulfillment(): FulfillmentType
    fun getShippingAddress(): Address?
    fun getBillingAddress(): Address?

    fun setBillingAddress(billingAddress: Address, basketDataRepository: BasketDataRepository)
    fun setShippingAddress(shippingAddress: Address, basketDataRepository: BasketDataRepository)
    fun setFulfillment(
        fulfillment: FulfillmentType, fulfillmentPort: FulfillmentPort,
        basketDataRepository: BasketDataRepository,
    )

    fun setCustomer(customer: Customer, basketDataRepository: BasketDataRepository)
    fun validate(): ValidationResult
    fun getOutdated(): Boolean
    fun setOutdated(outdated: Boolean)
}
