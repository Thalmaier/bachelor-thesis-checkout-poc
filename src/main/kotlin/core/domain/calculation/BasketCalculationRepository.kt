package core.domain.calculation

import core.domain.basketdata.model.BasketId
import core.domain.calculation.model.BasketCalculation
import core.domain.common.Port

/**
 * Repository for handling access to the database for a [BasketCalculation] entity
 */
@Port
interface BasketCalculationRepository {

    /**
     * Stores the [BasketCalculation] in the database
     */
    fun save(basketCalculation: BasketCalculation)

    fun findStaleBasketCalculation(id: BasketId): BasketCalculation

}
