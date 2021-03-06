package core.domain.calculation.model

import core.domain.basket.model.BasketItem
import core.domain.shipping.model.ShippingCost
import core.domain.shipping.service.ShippingCostService

/**
 * Represents the result of a calculation of a [BasketItem]
 */
data class BasketItemCalculationResult(
    val itemCost: CalculationResult = CalculationResult(),
    val shippingCost: ShippingCost = ShippingCostService.ZERO_MONEY,
    val totalCost: CalculationResult = itemCost,
) {

    /**
     * Adds two [BasketItemCalculationResult] together
     */
    fun add(calculationResult: BasketItemCalculationResult): BasketItemCalculationResult {
        return BasketItemCalculationResult(
            itemCost = itemCost.add(calculationResult.itemCost),
            shippingCost = shippingCost.add(calculationResult.shippingCost),
            totalCost = totalCost.add(calculationResult.totalCost)
        )
    }

}

/**
 * Transforms a [BasketItemCalculationResult] into a [BasketCalculationResult]
 */
fun BasketItemCalculationResult?.toBasketCalculationResult(): BasketCalculationResult {
    return when {
        this == null -> BasketCalculationResult()
        else -> BasketCalculationResult(
            grandTotal = itemCost.grossAmount.add(shippingCost),
            netTotal = itemCost.netAmount,
            shippingCostTotal = shippingCost,
            vatAmounts = totalCost.vatAmounts
        )
    }
}


/**
 * Combines a list of [BasketItemCalculationResult] into one [BasketItemCalculationResult]
 */
fun Collection<BasketItemCalculationResult>.combine(): BasketItemCalculationResult? =
    this.reduceOrNull { result1, result2 -> result1.add(result2) }
