package secondary.adapter.payment

import core.domain.basket.model.Basket
import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.OutletId
import javax.money.MonetaryAmount

/**
 * DTO for calling [PaymentAdapter.determineAvailablePaymentMethods]
 */
data class AvailablePaymentInformation(
    val outletId: OutletId,
    val amountToPay: MonetaryAmount,
    val fulfillmentType: FulfillmentType,
) {
    constructor(basket: Basket) : this(
        outletId = basket.getOutletId(),
        amountToPay = basket.getCalculationResult().grandTotal,
        fulfillmentType = basket.getFulfillment()
    )
}
