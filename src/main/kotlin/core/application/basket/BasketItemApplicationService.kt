package core.application.basket

import core.application.ApplicationService
import core.application.pricing.PricingService
import core.application.product.ProductService
import core.domain.basket.BasketRepository
import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.basket.model.BasketItem
import core.domain.basket.model.BasketItemId
import core.domain.common.Transaction
import core.domain.common.throwIf
import core.domain.exception.BadParameterError
import core.domain.exception.ResourceNotFoundError
import core.domain.product.model.ProductId
import core.domain.shipping.service.ShippingCostService
import mu.KotlinLogging
import kotlin.math.absoluteValue

/**
 * Implementation of the [BasketItemApiPort]
 */
@ApplicationService
class BasketItemApplicationService(
    private val basketRepository: BasketRepository,
    private val productService: ProductService,
    private val pricingService: PricingService,
    private val shippingCostService: ShippingCostService,
) : BasketItemApiPort {

    private val logger = KotlinLogging.logger {}

    override fun addBasketItem(basketId: BasketId, productId: ProductId): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                addBasketItem(basket, productId, true)
            }
        }
    }

    private fun addBasketItem(basket: Basket, productId: ProductId, calculateAndSave: Boolean = true) {
        logger.info { "Add product $productId to basket ${basket.getBasketId()}" }
        val product = productService.fetchProductInformation(productId)
        val price = pricingService.fetchPriceInformation(basket.getOutletId(), productId)
        if (calculateAndSave) {
            basket.addBasketItemAndRecalculate(product, price, shippingCostService)
            basketRepository.save(basket)
            logger.info { "Saved basket ${basket.getBasketId()} with new item" }
        } else {
            basket.addBasketItem(product, price)
        }
    }

    override fun removeBasketItem(basketId: BasketId, basketItemId: BasketItemId): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                removeBasketItem(basket, basketItemId, true)
            }
        }
    }

    private fun removeBasketItem(basket: Basket, basketItemId: BasketItemId, calculateAndSave: Boolean = true) {
        logger.info { "Remove item $basketItemId from basket ${basket.getBasketId()}" }
        if (calculateAndSave) {
            basket.removeBasketItem(basketItemId)
            basketRepository.save(basket)
            logger.info { "Saved basket ${basket.getBasketId()} after deleting a item" }
        } else {
            basket.removeBasketItem(basketItemId, false)
        }
    }

    override fun setBasketItemQuantity(basketId: BasketId, basketItemId: BasketItemId, quantity: Int): Basket {
        throwIf(quantity < 0) { BadParameterError("quantity is not allowed to be negative") }
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                logger.info { "Set quantity of item $basketItemId for basket $basketId to $quantity" }
                val sameProductItems = basket.getBasketItemsWithSameProduct(basketItemId)
                throwIf(sameProductItems.isEmpty()) { ResourceNotFoundError("basketItem", basketItemId) }

                val difference = sameProductItems.size - quantity
                adjustQuantity(basket, difference, sameProductItems)

                if (difference > 0) {
                    basket.updateBasketCalculationResultAndPaymentProcess()
                    basketRepository.save(basket)
                } else if (difference < 0) {
                    basket.calculateAndUpdate(shippingCostService)
                    basketRepository.save(basket)
                }
            }
        }
    }

    private fun adjustQuantity(basket: Basket, difference: Int, items: List<BasketItem>) {
        logger.info { "Adjust Quantity by $difference" }
        try {
            when {
                difference > 0 -> {
                    items.take(difference - 1).forEach { item ->
                        removeBasketItem(basket, item.id, calculateAndSave = false)
                    }
                    removeBasketItem(basket, items.last().id, calculateAndSave = true)
                }
                difference < 0 -> {
                    repeat(difference.absoluteValue - 1) {
                        addBasketItem(basket, items.first().getProductId(), calculateAndSave = false)
                    }
                    addBasketItem(basket, items.first().getProductId(), calculateAndSave = true)
                }
            }
        } catch (e: Exception) {
            /*
            Save basket if an exception occurred. This can happen because the to-be-added amount of products would be more than allowed.
            We still want to save the amount that was added until that moment.
             */
            logger.info { "Adjusting the quantity failed with exception, saving the basket to preserve changes due to message: ${e.message}" }
            basket.calculateAndUpdate(shippingCostService)
            basketRepository.save(basket)
        }
    }

}
