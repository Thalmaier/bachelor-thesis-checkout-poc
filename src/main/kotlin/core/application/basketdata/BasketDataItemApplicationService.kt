package core.application.basketdata

import core.application.ApplicationService
import core.application.pricing.PricingService
import core.application.product.ProductService
import core.domain.aggregate.Aggregates
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.BasketItem
import core.domain.basketdata.model.BasketItemId
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.common.Transaction
import core.domain.common.throwIf
import core.domain.exception.BadParameterError
import core.domain.exception.ResourceNotFoundError
import core.domain.product.model.ProductId
import core.domain.shipping.service.ShippingCostService
import mu.KotlinLogging
import kotlin.math.absoluteValue

/**
 * Implementation of the [BasketDataItemApiPort]
 */
@ApplicationService
class BasketDataItemApplicationService(
    private val basketDataRepository: BasketDataRepository,
    private val productService: ProductService,
    private val pricingService: PricingService,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val shippingCostsService: ShippingCostService,
    private val basketDataRefreshService: BasketDataRefreshService,
) : BasketDataItemApiPort {

    private val logger = KotlinLogging.logger {}

    override fun addBasketItem(basketId: BasketId, productId: ProductId): Aggregates {
        return Transaction {
            basketDataRefreshService.getRefreshedBasketData(basketId).let { basketData ->
                addBasketItem(basketData, productId, true)
                Aggregates(basketData)
            }
        }
    }

    private fun addBasketItem(basketData: BasketData, productId: ProductId, calculateAndSave: Boolean = true) {
        logger.info { "Add product $productId to basket ${basketData.getBasketId()}" }
        val product = productService.fetchProductInformation(productId)
        val price = pricingService.fetchPriceInformation(basketData.getOutletId(), productId)
        if (calculateAndSave) {
            val checkoutData = checkoutDataRepository.findCheckoutData(basketData.getBasketId())
            basketData.addBasketItemAndRecalculate(product, price, checkoutData, shippingCostsService)
            basketDataRepository.save(basketData)
            logger.info { "Saved basket ${basketData.getBasketId()} with new item" }
        } else {
            basketData.addBasketItem(product, price)
        }
    }

    override fun removeBasketItem(basketId: BasketId, basketItemId: BasketItemId): Aggregates {
        return Transaction {
            basketDataRefreshService.getRefreshedBasketData(basketId).let { basketData ->
                removeBasketItem(basketData, basketItemId, true)
                Aggregates(basketData)
            }
        }
    }

    private fun removeBasketItem(basketData: BasketData, basketItemId: BasketItemId, save: Boolean = true) {
        logger.info { "Remove item $basketItemId from basket ${basketData.getBasketId()}" }
        if (save) {
            basketData.removeBasketItem(basketItemId)
            basketDataRepository.save(basketData)
            logger.info { "Saved basket ${basketData.getBasketId()} after deleting a item" }
        } else {
            basketData.removeBasketItem(basketItemId)
        }
    }

    override fun setBasketItemQuantity(basketId: BasketId, basketItemId: BasketItemId, quantity: Int): Aggregates {
        throwIf(quantity < 0) { BadParameterError("quantity is not allowed to be negative") }
        return Transaction {
            basketDataRefreshService.getRefreshedBasketData(basketId).let { basketData ->
                logger.info { "Set quantity of item $basketItemId for basket $basketId to $quantity" }
                val sameProductItems = basketData.getBasketItemsWithSameProduct(basketItemId)
                throwIf(sameProductItems.isEmpty()) { ResourceNotFoundError("basketItem", basketItemId) }

                val difference = sameProductItems.size - quantity
                adjustQuantity(basketData, difference, sameProductItems)

                if (difference != 0) {
                    basketDataRepository.save(basketData)
                }

                Aggregates(basketData)
            }
        }
    }

    private fun adjustQuantity(basketData: BasketData, difference: Int, items: List<BasketItem>) {
        logger.info { "Adjust Quantity by $difference" }
        try {
            when {
                difference > 0 -> repeat(difference) {
                    items.take(difference).forEach { item ->
                        removeBasketItem(basketData, item.id, save = false)
                    }
                }
                difference < 0 -> {
                    repeat(difference.absoluteValue - 1) {
                        addBasketItem(basketData, items.first().getProductId(), calculateAndSave = false)
                    }
                    addBasketItem(basketData, items.first().getProductId(), calculateAndSave = true)
                }
            }
        } catch (e: Exception) {
            /*
            Save basket if an exception occurred. This can happen because the to-be-added amount of products would be more than allowed.
            We still want to save the amount that was added until that moment.
             */
            logger.info { "Adjusting the quantity failed with exception, saving the basket to preserve changes due to message: ${e.message}" }
            val checkoutData = checkoutDataRepository.findCheckoutData(basketData.getBasketId())
            val shippingCosts = shippingCostsService.calculateShippingCost(basketData, checkoutData)
            basketData.calculateBasketItemsAndUpdateShippingCost(shippingCosts)
            basketDataRepository.save(basketData)
        }
    }

}
