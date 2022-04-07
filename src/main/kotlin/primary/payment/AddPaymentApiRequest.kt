package primary.payment

import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentMethod
import core.domain.shipping.service.ShippingCostService
import javax.money.MonetaryAmount

data class AddPaymentApiRequest(
    val method: PaymentMethod,
    val amountSelected: MonetaryAmount?,
) {
    fun toPayment(): Payment {
        return Payment(method = method, amountSelected = amountSelected ?: ShippingCostService.ZERO_MONEY)
    }
}
