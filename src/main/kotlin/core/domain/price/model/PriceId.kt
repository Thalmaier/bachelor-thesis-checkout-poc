package core.domain.price.model

import core.domain.basket.model.OutletId
import core.domain.product.model.ProductId

/**
 * Value object for an id of a [Price] consisting of the [OutletId] and [ProductId]
 */
data class PriceId(
    val outletId: OutletId,
    val productId: ProductId,
)
