package core.domain.order.service

import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketStatus
import core.domain.common.DomainService
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.order.OrderPort
import core.domain.payment.model.PaymentProcess
import mu.KotlinLogging

@DomainService
class OrderDomainService(
    private val orderPort: OrderPort,
    private val basketDataRepository: BasketDataRepository,
) : OrderService {

    private val logger = KotlinLogging.logger {}

    override fun createOrder(basketData: BasketData, paymentProcess: PaymentProcess) {
        throwIf(basketData.getStatus() != BasketStatus.FINALIZED) {
            IllegalModificationError("Cannot create order if basket is not finalized")
        }
        throwIf(!paymentProcess.isInitialized()) {
            IllegalModificationError("Cannot create order if basket payment process is not created")
        }
        logger.info { "Create order for basket ${basketData.getBasketId()}" }
        val orderData = orderPort.createOrder(basketData, paymentProcess)
        basketData.setOrder(orderData)
        basketDataRepository.save(basketData)
    }

}
