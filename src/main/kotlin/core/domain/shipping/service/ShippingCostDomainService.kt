package core.domain.shipping.service

import core.domain.basket.model.Basket
import core.domain.basket.model.FulfillmentType
import core.domain.common.DomainService
import core.domain.shipping.ShippingPort
import core.domain.shipping.model.ProductsShippingCost
import mu.KotlinLogging

@DomainService
class ShippingCostDomainService(
    private val shippingPort: ShippingPort,
) : ShippingCostService {

    private val logger = KotlinLogging.logger {}

    override fun calculateShippingCost(basket: Basket): ProductsShippingCost {
        logger.info { "Calculate shipping cost for basket ${basket.getBasketId()}" }

        return when (basket.getFulfillment()) {
            FulfillmentType.PICKUP -> emptyMap()
            else -> shippingPort.determineShippingCosts(basket)
        }
    }

}
