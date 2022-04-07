package core.domain.payment.service

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.common.Port
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcessAggregate

/**
 * Port for handling all payment api request
 */
@Port
interface PaymentProcessApiPort {

    fun getPaymentProcess(basketId: BasketId): Aggregates

    /**
     * Add a payment to the basket
     */
    fun addPayment(basketId: BasketId, payment: Payment): Aggregates

    /**
     * Returns a list of all available [PaymentMethod]s
     */
    fun getAvailablePaymentMethods(basketId: BasketId): Set<PaymentMethod>

    /**
     * Initializes the payment process of a [BasketData] and freezes the [BasketData]
     */
    fun initializePaymentProcessAndFreezeBasket(basketId: BasketId): Aggregates

    /**
     * Executes the payment process of a [BasketData]
     */
    fun executePaymentProcessAndFinalizeBasket(basketId: BasketId): Aggregates

    /**
     * Cancels a payment of a [PaymentProcessAggregate]
     */
    fun cancelPayment(basketId: BasketId, paymentId: PaymentId): Aggregates

    /**
     * Cancels a payment process for a [BasketData] and resets the [BasketData]
     */
    fun cancelPaymentProcessAndResetBasket(basketId: BasketId): Aggregates
}
