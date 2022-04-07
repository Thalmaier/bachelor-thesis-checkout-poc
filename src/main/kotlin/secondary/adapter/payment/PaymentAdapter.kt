package secondary.adapter.payment

import core.domain.basketdata.model.BasketData
import core.domain.calculation.model.BasketCalculation
import core.domain.checkoutdata.model.CheckoutData
import core.domain.payment.PaymentPort
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcess
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [PaymentPort]
 */
@SecondaryAdapter
class PaymentAdapter(
    private val paymentApiService: PaymentApiService,
) : PaymentPort {

    override fun determineAvailablePaymentMethods(
        basketData: BasketData, checkoutData: CheckoutData,
        basketCalculation: BasketCalculation,
    ): Set<PaymentMethod> {
        return paymentApiService.determineAvailablePaymentMethods(
            AvailablePaymentInformation(basketData, checkoutData, basketCalculation)
        )
    }

    override fun createPaymentProcess(
        basketData: BasketData, paymentProcess: PaymentProcess,
        checkoutData: CheckoutData,
    ): ExternalPaymentRef {
        return paymentApiService.createPaymentProcess(
            CreatePaymentProcessInformation(basketData, checkoutData, paymentProcess)
        )
    }

    override fun initializeAllSubPayments(externalPaymentRef: ExternalPaymentRef, payments: Set<Payment>) {
        val initializePaymentInformation = InitializePaymentInformation(externalPaymentRef, payments)
        return paymentApiService.initializeAllSubPayments(initializePaymentInformation)
    }

    override fun executePayment(paymentRef: ExternalPaymentRef) {
        return paymentApiService.executePayment(paymentRef)
    }


}
