package core.domain.price.model

import config.Config
import config.PriceConfig
import core.domain.basket.model.BasketItem
import core.domain.validation.model.ValidationResult
import core.domain.validation.requiresRefreshIf
import secondary.common.TimeUtils
import java.time.LocalDateTime
import javax.money.MonetaryAmount

/**
 * Value object for containing all pricing data of a [BasketItem]
 */
data class Price(
    val id: PriceId,
    val grossAmount: MonetaryAmount,
    val updatedAt: LocalDateTime = TimeUtils.dateTimeNow(),
) {

    /**
     * Validates the [Price] if it still up to date
     */
    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.price"
        result.addResults(
            requiresRefreshIf(node, "updatedAt", requiresRefresh())
        )
    }

    /**
     * Returns true if the price is older than the configured [PriceConfig.updatePriceAfterSeconds]
     */
    fun requiresRefresh(): Boolean {
        return TimeUtils.olderThan(updatedAt, Config().price.updatePriceAfterSeconds)
    }

}
