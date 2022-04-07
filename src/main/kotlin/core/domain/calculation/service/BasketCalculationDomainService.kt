package core.domain.calculation.service

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
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
    private val paymentProcessRepository: PaymentProcessRepository,
    private val basketCalculationRepository: BasketCalculationRepository,
) : BasketCalculationService {

    override fun recalculateIfNecessaryAndSave(
        basketId: BasketId, basketData: BasketData, checkoutData: CheckoutData?,
        paymentProcess: PaymentProcess?,
    ): BasketCalculation {
        return Transaction {
            if (!basketData.canBeModified()) {
                return@Transaction basketCalculationRepository.findBasketCalculation(basketId)
            }

            val calculationResult = calculate(basketData)
            basketCalculationRepository.save(calculationResult)

            recalculatePaymentProcess(basketId, calculationResult, paymentProcess, basketData)

            return@Transaction calculationResult
        }
    }

    private fun recalculatePaymentProcess(
        basketId: BasketId, calculation: BasketCalculation,
        paymentProcess: PaymentProcess?, basketData: BasketData,
    ) {
        val paymentProcess = paymentProcess ?: paymentProcessRepository.findPaymentProcess(basketId)
        paymentProcess.calculate(calculation.getGrandTotal(), basketData)
        paymentProcessRepository.save(paymentProcess)
    }

    private fun calculate(basketData: BasketData): BasketCalculation {
        return BasketCalculationAggregate(basketData)
    }

}
