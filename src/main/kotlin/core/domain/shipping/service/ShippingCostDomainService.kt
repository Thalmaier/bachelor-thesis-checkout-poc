package core.domain.shipping.service

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.FulfillmentType
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.DomainService
import core.domain.shipping.ShippingPort
import core.domain.shipping.model.ProductsShippingCost
import mu.KotlinLogging

@DomainService
class ShippingCostDomainService(
    private val shippingPort: ShippingPort,
) : ShippingCostService {

    private val logger = KotlinLogging.logger {}

    override fun calculateShippingCost(basketData: BasketData, checkoutData: CheckoutData): ProductsShippingCost {
        logger.info { "Calculate shipping cost for basket ${basketData.getBasketId()}" }

        return when (checkoutData.getFulfillment()) {
            FulfillmentType.PICKUP -> emptyMap()
            else -> shippingPort.determineShippingCosts(basketData, checkoutData)
        }
    }

}
