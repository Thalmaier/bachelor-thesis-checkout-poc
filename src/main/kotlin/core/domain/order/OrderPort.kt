package core.domain.order

import core.domain.basketdata.model.BasketData
import core.domain.common.Port
import core.domain.order.model.Order
import core.domain.payment.model.PaymentProcess

/**
 * Port for communication with the order adapter
 */
@Port
interface OrderPort {

    /**
     * Creates a order in the external system and returns the reference on it
     */
    fun createOrder(basketData: BasketData, paymentProcess: PaymentProcess): Order

}
