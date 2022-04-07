package core.application.calculation

import core.application.ApplicationService
import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketId
import core.domain.calculation.BasketCalculationRepository
import core.domain.common.Transaction

@ApplicationService
class BasketCalculationApplicationService(
    private val basketCalculationRepository: BasketCalculationRepository,
) : BasketCalculationApiPort {
    override fun findBasketCalculationById(basketId: BasketId): Aggregates {
        return Transaction {
            Aggregates(
                basketCalculationRepository.findBasketCalculation(basketId)
            )
        }
    }
}
