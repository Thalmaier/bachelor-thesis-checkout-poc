package core.domain.payment.service

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.common.Port
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcess

/**
 * Port for handling all payment api request
 */
@Port
interface PaymentApiPort {

    /**
     * Add a payment to the basket
     */
    fun addPaymentToBasket(basketId: BasketId, payment: Payment): Basket

    /**
     * Returns a list of all available [PaymentMethod]s
     */
    fun getAvailablePaymentMethods(basketId: BasketId): Set<PaymentMethod>

    /**
     * Initializes the payment process of a [Basket] and freezes the [Basket]
     */
    fun initializePaymentProcessAndFreezeBasket(basketId: BasketId): Basket

    /**
     * Executes the payment process of a [Basket]
     */
    fun executePaymentProcessAndFinalizeBasket(basketId: BasketId): Basket

    /**
     * Cancels a payment of a [PaymentProcess]
     */
    fun cancelPayment(basketId: BasketId, paymentId: PaymentId): Basket

    /**
     * Cancels a payment process for a [Basket] and resets the [Basket]
     */
    fun cancelPaymentProcessAndResetBasket(basketId: BasketId): Basket
}
