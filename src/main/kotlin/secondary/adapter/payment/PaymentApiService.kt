package secondary.adapter.payment

import core.domain.basketdata.model.FulfillmentType
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.PaymentMethod
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the payment api
 */
class PaymentApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Get all available payment methods for the passed [AvailablePaymentInformation]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun determineAvailablePaymentMethods(paymentInformation: AvailablePaymentInformation): Set<PaymentMethod> {
        logger.info { "Fetch available payment method from external system for $paymentInformation" }
        MockTimeoutService.timeout(40, "determineAvailablePaymentMethods")
        return when (paymentInformation.fulfillmentType) {
            FulfillmentType.DELIVERY -> setOf(PaymentMethod.CREDIT_CARD, PaymentMethod.PAYPAL)
            FulfillmentType.PICKUP -> setOf(PaymentMethod.CREDIT_CARD, PaymentMethod.PAYPAL, PaymentMethod.CASH)
        }
    }

    /**
     * Create a new [ExternalPaymentRef] by calling the external payment api
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun createPaymentProcess(paymentInformation: CreatePaymentProcessInformation): ExternalPaymentRef {
        logger.info { "Create a new payment process in the external payment system for $paymentInformation" }
        MockTimeoutService.timeout(50, "createPaymentProcess")
        return ExternalPaymentRef()
    }

    /**
     * Initializes the payment process on the side of the external payment api
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun initializeAllSubPayments(paymentInformation: InitializePaymentInformation) {
        MockTimeoutService.timeout(60, "initializeAllSubPayments")
        logger.info { "Initializing the payment for ${paymentInformation.externalPaymentRef} in the payment system" }
    }

    /**
     * Executes the payment process on the side of the external payment api
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun executePayment(externalPaymentRef: ExternalPaymentRef) {
        MockTimeoutService.timeout(60, "executePayment")
        logger.info { "Execute the payment for $externalPaymentRef in the payment system" }
    }

}
