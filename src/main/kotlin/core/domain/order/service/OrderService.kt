package core.domain.order.service

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketStatus

/**
 * Service for creating orders
 */
interface OrderService {

    /**
     * Create an order for a [finalized][BasketStatus.FINALIZED] [Basket]
     */
    fun createOrder(basket: Basket)

}