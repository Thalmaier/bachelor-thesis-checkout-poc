package secondary.adapter.order

import core.domain.basket.model.Basket
import core.domain.order.model.Order
import core.domain.order.model.OrderRef
import mu.KotlinLogging
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the order api
 */
class OrderApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Create a new order for a finalized [Basket]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun createOrder(basket: Basket): Order {
        logger.info { "Create order for basket ${basket.getBasketId()}" }
        MockTimeoutService.timeout(35, "createOrder")
        return Order(orderRef = OrderRef())
    }

}