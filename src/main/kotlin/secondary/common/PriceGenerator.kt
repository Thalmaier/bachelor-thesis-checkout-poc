package core.domain.price

import core.domain.shipping.service.ShippingCostService
import javax.money.Monetary
import javax.money.MonetaryAmount
import kotlin.random.Random

/**
 * Generates a random amount of money. Used for mocking external systems.
 */
fun generateRandomMonetaryAmount(maxAmount: Double = 100.0): MonetaryAmount {
    return Monetary
        .getDefaultAmountFactory()
        .setCurrency(Monetary.getCurrency("EUR"))
        .setNumber(Random.nextDouble(0.0, maxAmount))
        .create().with(ShippingCostService.getRoundingOptions())
}