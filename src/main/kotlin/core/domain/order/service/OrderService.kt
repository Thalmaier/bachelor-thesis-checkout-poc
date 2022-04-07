package core.domain.order.service

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketStatus
import core.domain.payment.model.PaymentProcess

/**
 * Service for creating orders
 */
interface OrderService {

    /**
     * Create an order for a [finalized][BasketStatus.FINALIZED] [BasketData] and [PaymentProcess]
     */
    fun createOrder(basketData: BasketData, paymentProcess: PaymentProcess)

}
