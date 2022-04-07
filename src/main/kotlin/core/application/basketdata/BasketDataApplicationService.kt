package core.application.basketdata

import core.application.ApplicationService
import core.domain.aggregate.Aggregates
import core.domain.basketdata.BasketDataFactory
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.OutletId
import core.domain.basketdata.model.customer.Customer
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.checkoutdata.CheckoutDataFactory
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.common.Transaction
import mu.KotlinLogging

/**
 * Implementation of the [BasketApiPort]
 */
@ApplicationService
class BasketDataApplicationService(
    private val basketDataRepository: BasketDataRepository,
    private val fulfillmentPort: FulfillmentPort,
    private val basketDataFactory: BasketDataFactory,
    private val checkoutDataFactory: CheckoutDataFactory,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val basketDataRefreshService: BasketDataRefreshService,
) : BasketDataApiPort {

    private val logger = KotlinLogging.logger {}

    override fun findBasketDataById(basketId: BasketId): Aggregates {
        return Transaction {
            Aggregates(basketDataRefreshService.getRefreshedBasketData(basketId))
        }
    }

    override fun createBasket(outletId: OutletId, customer: Customer?): Aggregates {
        return basketDataFactory.createNewBasketData(outletId = outletId).let { basketData ->
            Transaction {
                basketDataRepository.save(basketData)
                logger.info { "Saved new basket with id ${basketData.getBasketId()}" }
                if (customer != null) {
                    val checkoutData = checkoutDataFactory.createNewCheckoutData(basketData.getBasketId(), customer)
                    checkoutDataRepository.save(checkoutData)
                    Aggregates(basketData = basketData, checkoutData = checkoutData)
                }
                Aggregates(basketData = basketData)
            }
        }
    }

    override fun cancelBasket(basketId: BasketId): Aggregates {
        return Transaction {
            basketDataRepository.findBasketData(basketId).let { basketData ->
                basketData.cancel()
                basketDataRepository.save(basketData)
                logger.info { "Canceled basket with id ${basketData.getBasketId()}" }
                Aggregates(basketData)
            }
        }
    }

    override fun getAvailableFulfillment(basketId: BasketId): List<FulfillmentType> {
        return Transaction {
            val basket = basketDataRepository.findBasketData(basketId)
            fulfillmentPort.getPossibleFulfillment(basket.getOutletId()).also {
                logger.info { "Fetched available fulfillment for basket ${basket.getBasketId()}" }
            }
        }
    }


}
