package core.domain.basketdata.service

import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.common.DomainService
import core.domain.common.ModifiedResult
import core.domain.price.PricePort
import core.domain.product.ProductPort
import core.domain.shipping.service.ShippingCostService
import mu.KotlinLogging

/**
 * Implementation of the [BasketDataRefreshService]
 */
@DomainService
class BasketDataRefreshDomainService(
    private val pricePort: PricePort,
    private val productPort: ProductPort,
    private val shippingCostService: ShippingCostService,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val basketDataRepository: BasketDataRepository,
    private val basketCalculationService: BasketCalculationService,
) : BasketDataRefreshService {

    private val logger = KotlinLogging.logger {}

    override fun getRefreshedBasketData(basketId: BasketId): BasketData {
        val staleBasketData = basketDataRepository.findBasketData(basketId)
        refreshAndUpdateBasketDataWithoutSaving(staleBasketData).also { result ->
            if (result.modified) {
                basketDataRepository.save(staleBasketData)
            }
        }
        return staleBasketData
    }

    override fun refreshAndUpdateBasketDataWithoutSaving(basketData: BasketData): ModifiedResult<BasketData> {
        if (!basketData.canBeModified() || basketData.getItems().isEmpty()) {
            return ModifiedResult.Unchanged(basketData)
        }

        logger.info { "Check if basket needs a refresh" }
        val refreshedPrices = refreshPricesIfNecessary(basketData).modified
        val refreshedProducts = refreshProductIfNecessary(basketData).modified

        if (refreshedPrices || refreshedProducts) {
            val checkoutData = checkoutDataRepository.findCheckoutData(basketData.getBasketId())
            basketData.updateShippingCostAndRecalculateBasket(
                shippingCostService.calculateShippingCost(basketData, checkoutData),
                basketCalculationService
            )
            return ModifiedResult.Updated(basketData)
        }

        return ModifiedResult.Unchanged(basketData)
    }

    private fun refreshPricesIfNecessary(basketData: BasketData): ModifiedResult<BasketData> {
        return when (basketData.requiresPriceRefresh()) {
            true -> basketData.refreshPrices(pricePort)
            false -> ModifiedResult.Unchanged(basketData)
        }
    }

    private fun refreshProductIfNecessary(basketData: BasketData): ModifiedResult<BasketData> {
        return when (basketData.requiresProductRefresh()) {
            true -> basketData.refreshProducts(productPort)
            false -> ModifiedResult.Unchanged(basketData)
        }
    }

}
