package core.domain.aggregate

import com.fasterxml.jackson.annotation.JsonInclude
import core.domain.basketdata.model.BasketData
import core.domain.calculation.model.BasketCalculation
import core.domain.checkoutdata.model.CheckoutData
import core.domain.payment.model.PaymentProcess

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Aggregates(
    val basketData: BasketData? = null,
    val checkoutData: CheckoutData? = null,
    val paymentProcess: PaymentProcess? = null,
    val basketCalculation: BasketCalculation? = null,
) {

    constructor(basketData: BasketData) : this(basketData = basketData, checkoutData = null)
    constructor(checkoutData: CheckoutData) : this(basketData = null, checkoutData = checkoutData)
    constructor(paymentProcess: PaymentProcess) : this(basketData = null, paymentProcess = paymentProcess)
    constructor(basketCalculation: BasketCalculation) : this(basketData = null, basketCalculation = basketCalculation)

}
