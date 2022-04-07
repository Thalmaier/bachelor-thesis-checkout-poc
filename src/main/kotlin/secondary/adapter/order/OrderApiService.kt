package secondary.adapter.order

import core.domain.basketdata.model.BasketData
import core.domain.order.model.Order
import core.domain.order.model.OrderRef
import core.domain.payment.model.PaymentProcess
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the order api
 */
class OrderApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Create a new order for a finalized [BasketData]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun createOrder(basketData: BasketData, paymentProcess: PaymentProcess): Order {
        logger.info { "Create order for basket ${basketData.getBasketId()}" }
        MockTimeoutService.timeout(35, "createOrder")
        return Order(orderRef = OrderRef())
    }

}
