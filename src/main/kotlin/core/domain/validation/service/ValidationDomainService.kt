package core.domain.validation.service

import com.sksamuel.hoplite.fp.NonEmptyList
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.calculation.BasketCalculationRepository
import core.domain.calculation.model.BasketCalculation
import core.domain.calculation.model.BasketCalculationAggregate
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.DomainService
import core.domain.exception.ValidationError
import core.domain.payment.model.PaymentProcess
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.model.Invalid
import core.domain.validation.model.ValidationResult
import core.domain.validation.validIfEqual
import mu.KotlinLogging

@DomainService
class ValidationDomainService(
    private val basketDataRepository: BasketDataRepository,
    private val basketCalculationRepository: BasketCalculationRepository,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val basketDataRefreshService: BasketDataRefreshService,
    private val shippingCostService: ShippingCostService,
    private val basketCalculationService: BasketCalculationService,
) : ValidationService {

    private val logger = KotlinLogging.logger {}

    override fun validateAndThrowIfInvalid(basketData: BasketData, paymentProcess: PaymentProcess) {
        logger.info { "Validate basket for unsatisfied business rules" }
        val basketDataResult = basketData.validate()

        val paymentProcessResult = paymentProcess.validate()
        val basketCalculation = basketCalculationRepository.findBasketCalculation(basketData.getBasketId())
        val basketCalculationResult = basketCalculation.validate()
        val checkoutData = checkoutDataRepository.findCheckoutData(basketData.getBasketId())
        val checkoutDataResult = checkoutData.validate()
        val calculationOutdatedResult = validateIfCalculationWasOutdated(basketData, checkoutData, basketCalculation)
        val totalResult = basketDataResult.addResults(
            paymentProcessResult, basketCalculationResult, checkoutDataResult, calculationOutdatedResult
        )

        totalResult.onInvalid { refreshIfValidationFailedDueToRefreshRequired(it, basketData) }
        totalResult.throwIfInvalid { errors -> ValidationError(errors) }
    }

    /**
     * Validates if the [BasketCalculationAggregate] is up-to-date
     */
    private fun validateIfCalculationWasOutdated(
        basketData: BasketData, checkoutData: CheckoutData,
        basketCalculation: BasketCalculation,
    ): ValidationResult {
        if (basketData.canBeModified()) {
            val shippingCost = shippingCostService.calculateShippingCost(basketData, checkoutData)
            basketData.updateShippingCostAndRecalculateBasket(shippingCost, basketCalculationService)
        }

        val after = basketCalculationRepository.findBasketCalculation(basketData.getBasketId())

        return ValidationResult().addResults(
            validIfEqual(basketCalculation.getGrandTotal(), after.getGrandTotal(), "total is incorrect"),
            validIfEqual(
                basketCalculation.getShippingCostTotal(),
                after.getShippingCostTotal(),
                "shippingCostTotal is incorrect"
            ),
            validIfEqual(basketCalculation.getNetTotal(), after.getNetTotal(), "netTotal is incorrect"),
        )
    }

    private fun refreshIfValidationFailedDueToRefreshRequired(errors: NonEmptyList<Invalid>, basketData: BasketData) {
        if (errors.list.filterIsInstance<Invalid.RefreshRequired>().isNotEmpty()) {
            basketDataRefreshService.refreshAndUpdateBasketDataWithoutSaving(basketData)
            basketDataRepository.save(basketData)
        }
    }


}
