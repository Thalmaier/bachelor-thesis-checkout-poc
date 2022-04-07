package core.domain.order

import core.domain.basket.model.Basket
import core.domain.common.Port
import core.domain.order.model.Order

/**
 * Port for communication with the order adapter
 */
@Port
interface OrderPort {

    /**
     * Creates a [Order] in the external system and returns the reference on it
     */
    fun createOrder(basket: Basket): Order

}