package core.application.aggregate

import core.application.ApplicationService
import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.common.Transaction
import core.domain.payment.PaymentProcessRepository

/**
 * Implementation of the [AggregationApiPort]
 */
@ApplicationService
class AggregateApplicationService(
    private val basketDataRefreshService: BasketDataRefreshService,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val calculationService: BasketCalculationService,
    private val paymentProcessRepository: PaymentProcessRepository,
) : AggregationApiPort {

    override fun findBasketWithAllAggregates(basketId: BasketId): Aggregates {
        return Transaction {
            Aggregates(
                basketData = basketDataRefreshService.getRefreshedBasketData(basketId),
                checkoutData = checkoutDataRepository.findCheckoutData(basketId),
                basketCalculation = calculationService.getUpdatedCalculation(basketId),
                paymentProcess = paymentProcessRepository.findPaymentProcess(basketId)
            )
        }
    }


}
