package core.domain.basket.service

import core.domain.basket.model.Basket
import core.domain.common.ModifiedResult
import core.domain.price.model.Price
import core.domain.product.model.Product

/**
 * Service used to update a stale [Basket].
 */
interface BasketRefreshService {

    /**
     * Refreshes the stale [Basket] data like [Product] amd [Price].
     * If values where refreshed, it recalculates the basket and persist the data.
     */
    fun refreshBasketDataWithoutSaving(basket: Basket): ModifiedResult<Basket>

}
