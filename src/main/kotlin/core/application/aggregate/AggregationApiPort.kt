package core.application.aggregate

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketId

interface AggregationApiPort {

    fun findBasketWithAllAggregates(basketId: BasketId): Aggregates

}