package core.application.calculation

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketId
import core.domain.common.Port

@Port
interface BasketCalculationApiPort {

    fun findBasketCalculationById(basketId: BasketId): Aggregates

}
