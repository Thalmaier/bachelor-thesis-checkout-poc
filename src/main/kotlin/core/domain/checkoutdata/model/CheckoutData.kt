package core.domain.checkoutdata.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.Customer
import core.domain.calculation.service.BasketCalculationService
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.model.ValidationResult

interface CheckoutData {

    @JsonIgnore fun getBasketId(): BasketId
    fun getCustomer(): Customer?
    fun getFulfillment(): FulfillmentType
    fun getShippingAddress(): Address?
    fun getBillingAddress(): Address?

    fun setBillingAddress(billingAddress: Address, basketDataRepository: BasketDataRepository)
    fun setShippingAddress(
        shippingAddress: Address, basketDataRepository: BasketDataRepository,
        shippingCostService: ShippingCostService,
        basketCalculationService: BasketCalculationService,
    )

    fun setFulfillment(
        fulfillment: FulfillmentType, fulfillmentPort: FulfillmentPort,
        basketDataRepository: BasketDataRepository,
        shippingCostService: ShippingCostService,
        basketCalculationService: BasketCalculationService,
    )

    fun setCustomer(customer: Customer, basketDataRepository: BasketDataRepository)
    fun validate(): ValidationResult
}
