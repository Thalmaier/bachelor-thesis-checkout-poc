package core.domain.shipping.service

import config.Config
import core.domain.basketdata.model.BasketData
import core.domain.calculation.model.BasketCalculationAggregate
import core.domain.checkoutdata.model.CheckoutData
import core.domain.product.model.Vat
import core.domain.shipping.model.ProductsShippingCost
import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.math.RoundingMode
import javax.money.Monetary
import javax.money.MonetaryAmount
import javax.money.MonetaryOperator
import javax.money.RoundingQueryBuilder

/**
 * Interface for calculating a [BasketCalculationAggregate] or [ProductsShippingCost]
 */
interface ShippingCostService {

    companion object {
        val ZERO_MONEY: MonetaryAmount = Money.zero(Config().currency.currencyUnit)

        fun getRoundingOptions(): MonetaryOperator {
            return Monetary.getRounding(RoundingQueryBuilder.of().set(RoundingMode.HALF_UP).setScale(2).build())
        }

        /**
         * Calculates the amount of a [Vat] on the [MonetaryAmount]
         */
        fun calculateVatCost(grossAmount: MonetaryAmount, vat: Vat): MonetaryAmount {
            return grossAmount
                .divide(vat.rate.add(BigDecimal.valueOf(100L)))
                .multiply(vat.rate)
                .with(getRoundingOptions())
        }
    }

    /**
     * Calculates the [ProductsShippingCost] of a [BasketData]
     */
    fun calculateShippingCost(basketData: BasketData, checkoutData: CheckoutData): ProductsShippingCost
}
