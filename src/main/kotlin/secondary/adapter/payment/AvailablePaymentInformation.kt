package secondary.adapter.payment

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.OutletId
import core.domain.calculation.model.BasketCalculation
import core.domain.checkoutdata.model.CheckoutData
import javax.money.MonetaryAmount

/**
 * DTO for calling [PaymentAdapter.determineAvailablePaymentMethods]
 */
data class AvailablePaymentInformation(
    val outletId: OutletId,
    val amountToPay: MonetaryAmount,
    val fulfillmentType: FulfillmentType,
) {
    constructor(basketData: BasketData, checkoutData: CheckoutData, basketCalculation: BasketCalculation) : this(
        outletId = basketData.getOutletId(),
        amountToPay = basketCalculation.getGrandTotal(),
        fulfillmentType = checkoutData.getFulfillment()
    )
}
