package core.domain.basket

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketAggregate
import core.domain.basket.model.BasketId
import core.domain.basket.model.OutletId
import core.domain.basket.model.customer.Customer
import core.domain.common.Factory

/**
 * Factory for creating new [Basket] instances.
 * This class is primarily more of an example.
 */
@Factory
class BasketFactory {
    fun createNewBasket(id: BasketId = BasketId(), outletId: OutletId, customer: Customer?): Basket {
        return BasketAggregate(id = id, outletId = outletId, customer = customer)
    }
}