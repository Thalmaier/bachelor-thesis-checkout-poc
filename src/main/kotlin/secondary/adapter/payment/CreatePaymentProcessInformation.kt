package secondary.adapter.payment

import core.domain.basketdata.model.*
import core.domain.basketdata.model.customer.Customer
import core.domain.checkoutdata.model.CheckoutData
import core.domain.payment.model.PaymentProcess
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

    constructor(basketData: BasketData, checkoutData: CheckoutData, paymentProcess: PaymentProcess) : this(
        basketId = basketData.getBasketId(),
        outletId = basketData.getOutletId(),
        amountToPay = paymentProcess.getAmountToPay(),
        fulfillmentType = checkoutData.getFulfillment(),
        shippingAddress = checkoutData.getShippingAddress(),
        customer = checkoutData.getCustomer()
    )
}
