package secondary.adapter.payment

import core.domain.basket.model.*
import core.domain.basket.model.customer.Customer
import javax.money.MonetaryAmount

/**
 * DTO for calling [PaymentAdapter.createPaymentProcess]
 */
data class CreatePaymentProcessInformation(
    val basketId: BasketId,
    val outletId: OutletId,
    val amountToPay: MonetaryAmount,
    val fulfillmentType: FulfillmentType,
    val shippingAddress: Address?,
    val customer: Customer?,
) {

    constructor(basket: Basket) : this(
        basketId = basket.getBasketId(),
        outletId = basket.getOutletId(),
        amountToPay = basket.getCalculationResult().grandTotal,
        fulfillmentType = basket.getFulfillment(),
        shippingAddress = basket.getShippingAddress(),
        customer = basket.getCustomer()
    )
}
