package core.domain.calculation.model

import core.domain.basket.model.Basket
import core.domain.shipping.model.ShippingCost
import core.domain.shipping.service.ShippingCostService
import javax.money.MonetaryAmount

/**
 * Represents the result of a calculation of a [Basket]
 */
data class BasketCalculationResult(
    val grandTotal: MonetaryAmount,
    val netTotal: MonetaryAmount,
    val shippingCostTotal: ShippingCost,
    val vatAmounts: Map<Int, VatAmount>,
) {
    constructor() : this(
        ShippingCostService.ZERO_MONEY,
        ShippingCostService.ZERO_MONEY,
        ShippingCostService.ZERO_MONEY,
        emptyMap()
    )
}
