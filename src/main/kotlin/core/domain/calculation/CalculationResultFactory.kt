package core.domain.calculation

import core.domain.calculation.model.BasketItemCalculationResult
import core.domain.calculation.model.CalculationResult
import core.domain.calculation.model.VatAmount
import core.domain.common.Factory
import core.domain.price.model.Price
import core.domain.product.model.Vat
import core.domain.shipping.model.ShippingCost
import core.domain.shipping.service.ShippingCostService

/**
 * Factory to create [CalculationResult]s
 */
@Factory
object CalculationResultFactory {

    /**
     * Returns a [BasketItemCalculationResult] depending on a [Price], [Vat] and [ShippingCost]
     */
    fun basketItemResult(price: Price, vat: Vat, shippingCost: ShippingCost): BasketItemCalculationResult {
        val itemCost = calculationResult(price, vat)
        return BasketItemCalculationResult(
            itemCost = itemCost,
            shippingCost = shippingCost,
            totalCost = itemCost + shippingCost
        )
    }

    /**
     * Returns a [CalculationResult] depending on a [Price] and [Vat]
     */
    fun calculationResult(price: Price, vat: Vat): CalculationResult {
        val vatCost = ShippingCostService.calculateVatCost(price.grossAmount, vat)
        return CalculationResult(
            grossAmount = price.grossAmount,
            vatAmount = VatAmount(sign = vat.sign, rate = vat.rate, amount = vatCost),
            netAmount = price.grossAmount.subtract(vatCost)
        )
    }

}