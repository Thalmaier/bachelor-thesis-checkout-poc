package secondary.adapter.payment

import core.domain.basket.model.Basket
import core.domain.payment.PaymentPort
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentMethod
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [PaymentPort]
 */
@SecondaryAdapter
class PaymentAdapter(private val paymentApiService: PaymentApiService) : PaymentPort {

    override fun determineAvailablePaymentMethods(basket: Basket): Set<PaymentMethod> {
        return paymentApiService.determineAvailablePaymentMethods(AvailablePaymentInformation(basket))
    }

    override fun createPaymentProcess(basket: Basket): ExternalPaymentRef {
        return paymentApiService.createPaymentProcess(CreatePaymentProcessInformation(basket))
    }

    override fun initializeAllSubPayments(externalPaymentRef: ExternalPaymentRef, payments: Set<Payment>) {
        val initializePaymentInformation = InitializePaymentInformation(externalPaymentRef, payments)
        return paymentApiService.initializeAllSubPayments(initializePaymentInformation)
    }

    override fun executePayment(paymentRef: ExternalPaymentRef) {
        return paymentApiService.executePayment(paymentRef)
    }


}
