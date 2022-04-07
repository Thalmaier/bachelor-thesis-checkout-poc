package core.domain.calculation.model

import core.domain.shipping.service.ShippingCostService
import javax.money.MonetaryAmount

/**
 * Contains a result of a calculation with [grossAmount], [netAmount] amd [vatAmounts]
 */
data class CalculationResult(
    val grossAmount: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    val netAmount: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    val vatAmounts: Map<Int, VatAmount> = emptyMap(),
) {

    constructor(
        grossAmount: MonetaryAmount = ShippingCostService.ZERO_MONEY,
        netAmount: MonetaryAmount = ShippingCostService.ZERO_MONEY,
        vatAmount: VatAmount,
    ) : this(grossAmount, netAmount, mapOf(Pair(vatAmount.sign, vatAmount)))

    /**
     * Adds two [CalculationResult]s together
     */
    fun add(calculationResult: CalculationResult): CalculationResult {
        return CalculationResult(
            grossAmount = grossAmount.add(calculationResult.grossAmount),
            netAmount = netAmount.add(calculationResult.netAmount),
            vatAmounts = addVat(calculationResult.vatAmounts)
        )
    }

    /**
     * Adds two [vatAmounts] together. Only [VatAmount]s with the same sign can be combined.
     */
    private fun addVat(vats: Map<Int, VatAmount>): Map<Int, VatAmount> {
        return this.vatAmounts.toMutableMap().also { originalVat ->
            vats.forEach { vat ->
                if (originalVat.containsKey(vat.key)) {
                    originalVat[vat.key] = originalVat[vat.key]!!.add(vat.value.amount)
                } else {
                    originalVat[vat.key] = vat.value
                }
            }
        }
    }

    /**
     * Add a [MonetaryAmount] to the [netAmount] of a [CalculationResult]
     */
    operator fun plus(amount: MonetaryAmount): CalculationResult {
        // Technically not correct but calculation is done that way for simplicity sake
        return this.copy(netAmount = netAmount.add(amount))
    }

}

