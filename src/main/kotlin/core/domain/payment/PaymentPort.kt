package core.domain.payment

import core.domain.basketdata.model.BasketData
import core.domain.calculation.model.BasketCalculation
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.Port
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcess

/**
 * Port for communication with the payment adapter
 */
@Port
interface PaymentPort {

    /**
     * Returns a list of available [PaymentMethod]s depending on the passed [BasketData]
     */
    fun determineAvailablePaymentMethods(
        basketData: BasketData, checkoutData: CheckoutData,
        basketCalculation: BasketCalculation,
    ): Set<PaymentMethod>

    /**
     * Creates a new payment process and returns a [ExternalPaymentRef] for that payment process
     */
    fun createPaymentProcess(
        basketData: BasketData, paymentProcess: PaymentProcess,
        checkoutData: CheckoutData,
    ): ExternalPaymentRef

    /**
     * Initializes all passed payments for the payment process of the passed [ExternalPaymentRef]
     */
    fun initializeAllSubPayments(externalPaymentRef: ExternalPaymentRef, payments: Set<Payment>)

    /**
     * Executes the payment process of the passed [ExternalPaymentRef]
     */
    fun executePayment(paymentRef: ExternalPaymentRef)

}
