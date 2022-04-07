package secondary.adapter.order

import core.domain.basketdata.model.BasketData
import core.domain.order.OrderPort
import core.domain.order.model.Order
import core.domain.payment.model.PaymentProcess
import secondary.adapter.SecondaryAdapter

/**
 * Adapter for a [OrderPort]
 */
@SecondaryAdapter
class OrderAdapter(
    private val orderApiService: OrderApiService,
) : OrderPort {
    override fun createOrder(basketData: BasketData, paymentProcess: PaymentProcess): Order {
        return orderApiService.createOrder(basketData, paymentProcess)
    }
}
