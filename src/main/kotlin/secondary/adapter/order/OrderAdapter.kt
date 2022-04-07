package secondary.adapter.order

import core.domain.basket.model.Basket
import core.domain.order.OrderPort
import core.domain.order.model.Order
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [OrderPort]
 */
@SecondaryAdapter
class OrderAdapter(private val orderApiService: OrderApiService) : OrderPort {
    override fun createOrder(basket: Basket): Order {
        return orderApiService.createOrder(basket)
    }
}