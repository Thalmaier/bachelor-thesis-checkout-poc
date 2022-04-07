package core.domain.product.model

import config.Config
import config.ProductConfig
import core.domain.basket.model.BasketItem
import core.domain.validation.model.ValidationResult
import core.domain.validation.requiresRefreshIf
import secondary.common.TimeUtils
import java.time.LocalDateTime

/**
 * Value object for containing all product data of a [BasketItem]
 */
data class Product(
    val id: ProductId,
    val name: String,
    val vat: Vat,
    val updatedAt: LocalDateTime = TimeUtils.dateTimeNow(),
) {

    /**
     * Validates the [Product] if it still up to date
     */
    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.product"
        result.addResults(
            requiresRefreshIf(node, "updatedAt", requiresRefresh())
        )
    }

    /**
     * Returns true if the product data is older than the configured [ProductConfig.updateProductAfterSeconds]
     */
    fun requiresRefresh(): Boolean {
        return TimeUtils.olderThan(updatedAt, Config().product.updateProductAfterSeconds)
    }

}
