package core.domain.payment

import core.domain.basket.model.Basket
import core.domain.common.Port
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentMethod

/**
 * Port for communication with the payment adapter
 */
@Port
interface PaymentPort {

    /**
     * Returns a list of available [PaymentMethod]s depending on the passed [Basket]
     */
    fun determineAvailablePaymentMethods(basket: Basket): Set<PaymentMethod>

    /**
     * Creates a new payment process for a [Basket] and returns a [ExternalPaymentRef] for that payment process
     */
    fun createPaymentProcess(basket: Basket): ExternalPaymentRef

    /**
     * Initializes all passed payments for the payment process of the passed [ExternalPaymentRef]
     */
    fun initializeAllSubPayments(externalPaymentRef: ExternalPaymentRef, payments: Set<Payment>)

    /**
     * Executes the payment process of the passed [ExternalPaymentRef]
     */
    fun executePayment(paymentRef: ExternalPaymentRef)

}
