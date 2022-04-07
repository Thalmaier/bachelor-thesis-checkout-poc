package core.domain.basket.service

import core.domain.basket.model.Basket
import core.domain.common.DomainService
import core.domain.common.ModifiedResult
import core.domain.price.PricePort
import core.domain.product.ProductPort
import core.domain.shipping.service.ShippingCostService
import mu.KotlinLogging

/**
 * Implementation of the [BasketRefreshService]
 */
@DomainService
class BasketRefreshDomainService(
    private val pricePort: PricePort,
    private val productPort: ProductPort,
    private val shippingCostService: ShippingCostService,
) : BasketRefreshService {

    private val logger = KotlinLogging.logger {}

    override fun refreshBasketDataWithoutSaving(basket: Basket): ModifiedResult<Basket> {
        if (!basket.canBeModified() || basket.getItems().isEmpty()) {
            return ModifiedResult.Unchanged(basket)
        }

        logger.info { "Check if basket needs a refresh" }
        val pricesRefreshed = refreshPricesIfNecessary(basket).modified
        val productRefreshed = refreshProductIfNecessary(basket).modified

        if (pricesRefreshed || productRefreshed) {
            logger.info { "Basket was refreshed, recalculate basket" }
            basket.calculateAndUpdate(shippingCostService)
            return ModifiedResult.Updated(basket)
        }
        return ModifiedResult.Unchanged(basket)
    }

    private fun refreshPricesIfNecessary(basket: Basket): ModifiedResult<Basket> {
        return when (basket.requiresPriceRefresh()) {
            true -> basket.refreshPrices(pricePort)
            false -> ModifiedResult.Unchanged(basket)
        }
    }

    private fun refreshProductIfNecessary(basket: Basket): ModifiedResult<Basket> {
        return when (basket.requiresProductRefresh()) {
            true -> basket.refreshProducts(productPort)
            false -> ModifiedResult.Unchanged(basket)
        }
    }

}
