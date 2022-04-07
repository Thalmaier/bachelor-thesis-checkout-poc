package core.domain.basketdata

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.common.Port
import core.domain.price.model.Price
import core.domain.product.model.Product

/**
 * Repository for handling access to the database for a [BasketData] entity
 */
@Port
interface BasketDataRepository {

    /**
     * Returns a stale version of the [BasketData] for a [BasketId].
     * Data like [Price] or [Product] will not be updated.
     */
    fun findStaleBasketData(id: BasketId): BasketData

    /**
     * Stores the [BasketData] in the database
     */
    fun save(basketData: BasketData)

    fun resetOutdatedFlag(id: BasketId)

    fun resetOutdatedFlag(basketData: BasketData)
}
