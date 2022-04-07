package core.domain.order.service

import core.domain.basket.BasketRepository
import core.domain.basket.model.Basket
import core.domain.basket.model.BasketStatus
import core.domain.common.DomainService
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.order.OrderPort
import mu.KotlinLogging

@DomainService
class OrderDomainService(
    private val orderPort: OrderPort,
    private val basketRepository: BasketRepository,
) : OrderService {

    private val logger = KotlinLogging.logger {}

    override fun createOrder(basket: Basket) {
        throwIf(basket.getStatus() != BasketStatus.FINALIZED) {
            IllegalModificationError("Cannot create order if basket is not finalized")
        }
        throwIf(!basket.isPaymentInitialized()) {
            IllegalModificationError("Cannot create order if basket payment process is not created")
        }
        logger.info { "Create order for basket ${basket.getBasketId()}" }
        val orderData = orderPort.createOrder(basket)
        basket.setOrder(orderData)
        basketRepository.save(basket)
    }

}