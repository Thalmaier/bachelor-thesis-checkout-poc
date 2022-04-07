package core.domain.basketdata

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketDataAggregate
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.OutletId
import core.domain.common.Factory

/**
 * Factory for creating new [BasketData] instances.
 * This class is primarily more of an example.
 */
@Factory
class BasketDataFactory {
    fun createNewBasketData(id: BasketId = BasketId(), outletId: OutletId): BasketData {
        return BasketDataAggregate(id = id, outletId = outletId)
    }
}
