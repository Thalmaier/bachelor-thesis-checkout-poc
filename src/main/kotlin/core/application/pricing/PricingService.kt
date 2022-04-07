package core.application.pricing

import core.domain.basket.model.OutletId
import core.domain.price.model.Price
import core.domain.product.model.ProductId

/**
 * Service for handling all [Price] related tasks
 */
interface PricingService {

    /**
     * Returns the [Price] for the combination of a [OutletId] and [ProductId].
     * The external price receives updates over time, so calling this method with the same parameters,
     * can yield different results.
     */
    fun fetchPriceInformation(outletId: OutletId, productId: ProductId): Price

}
