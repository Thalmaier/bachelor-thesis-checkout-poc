package core.domain.payment

import core.domain.basketdata.model.BasketId
import core.domain.common.Port
import core.domain.payment.model.PaymentProcess

/**
 * Repository for handling access to the database for a [PaymentProcess] entity
 */
@Port
interface PaymentProcessRepository {

    /**
     * Returns a [PaymentProcess] based on the [BasketId]
     */
    fun findPaymentProcess(id: BasketId): PaymentProcess

    /**
     * Stores the [PaymentProcess] in the database
     */
    fun save(paymentProcess: PaymentProcess)

}
