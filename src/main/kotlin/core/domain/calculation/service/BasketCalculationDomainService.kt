package core.domain.calculation.service

import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.calculation.BasketCalculationRepository
import core.domain.calculation.model.BasketCalculation
import core.domain.calculation.model.BasketCalculationAggregate
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.DomainService
import core.domain.common.Transaction
import core.domain.payment.PaymentProcessRepository
import core.domain.payment.model.PaymentProcess

@DomainService
class BasketCalculationDomainService(
    private val basketDataRepository: BasketDataRepository,
    private val paymentProcessRepository: PaymentProcessRepository,
    private val basketCalculationRepository: BasketCalculationRepository,
    private val basketDataRefreshService: BasketDataRefreshService,
) : BasketCalculationService {

    override fun getUpdatedCalculation(id: BasketId): BasketCalculation {
        return recalculateIfNecessaryAndSave(id)
    }

    override fun recalculateIfNecessaryAndSave(
        basketId: BasketId, basketData: BasketData?, checkoutData: CheckoutData?,
        paymentProcess: PaymentProcess?,
    ): BasketCalculation {
        return Transaction {
            val basketData = basketData ?: basketDataRefreshService.getRefreshedBasketData(basketId)

            if (!basketData.getOutdated()) {
                return@Transaction basketCalculationRepository.findStaleBasketCalculation(basketId)
            }

            basketDataRepository.resetOutdatedFlag(basketData)

            val calculationResult = calculate(basketData)
            basketCalculationRepository.save(calculationResult)

            recalculatePaymentProcess(basketId, calculationResult, paymentProcess)

            return@Transaction calculationResult
        }
    }

    private fun recalculatePaymentProcess(basketId: BasketId, calculation: BasketCalculation, paymentProcess: PaymentProcess?) {
        val paymentProcess = paymentProcess ?: paymentProcessRepository.findPaymentProcess(basketId)
        paymentProcess.calculate(calculation.getGrandTotal())
        paymentProcessRepository.save(paymentProcess)
    }

    private fun calculate(basketData: BasketData): BasketCalculation {
        return BasketCalculationAggregate(basketData)
    }

}
