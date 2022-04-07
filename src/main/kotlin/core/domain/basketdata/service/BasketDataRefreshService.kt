package core.domain.basketdata.service

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.common.ModifiedResult
import core.domain.price.model.Price
import core.domain.product.model.Product

/**
 * Service used to update a stale [BasketData].
 */
interface BasketDataRefreshService {

    /**
     * Refreshes the stale [BasketData] data like [Product] amd [Price].
     * If values where refreshed, it recalculates the basket and returns the result without saving.
     */
    fun refreshAndUpdateBasketDataWithoutSaving(basketData: BasketData): ModifiedResult<BasketData>

    fun getRefreshedBasketData(basketId: BasketId): BasketData
}
