package core.domain.validation.service

import com.sksamuel.hoplite.fp.NonEmptyList
import core.domain.basket.BasketRepository
import core.domain.basket.model.Basket
import core.domain.basket.service.BasketRefreshService
import core.domain.calculation.model.BasketCalculationResult
import core.domain.common.DomainService
import core.domain.exception.ValidationError
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.Invalid
import core.domain.validation.model.ValidationResult
import core.domain.validation.validIfEqual
import mu.KotlinLogging

@DomainService
class ValidationDomainService(
    private val shippingCostService: ShippingCostService,
    private val basketRepository: BasketRepository,
    private val refreshService: BasketRefreshService,
) : ValidationService {

    private val logger = KotlinLogging.logger {}

    override fun validateAndThrowIfInvalid(basket: Basket) {
        logger.info { "Validate basket for unsatisfied business rules" }
        val result = basket.validateBasketState()
        validateCalculationAndStoreIfOutdated(basket, result)
        result.onInvalid { refreshIfValidationFailedDueToRefreshRequired(it, basket) }
        result.throwIfInvalid { errors -> ValidationError(errors) }
    }

    /**
     * Validates if the [BasketCalculationResult] is up-to-date
     */
    private fun validateCalculationAndStoreIfOutdated(basket: Basket, result: ValidationResult) {
        val before = basket.getCalculationResult()
        val updateResult = basket.calculateAndUpdate(shippingCostService)
        val after = basket.getCalculationResult()
        result.addResults(
            invalidIf("basket", "calculationResult", updateResult.modified, "calculation resulted in update"),
            validIfEqual(before.grandTotal, after.grandTotal, "total is incorrect"),
            validIfEqual(
                before.shippingCostTotal,
                after.shippingCostTotal,
                "shippingCostTotal is incorrect"
            ),
            validIfEqual(before.netTotal, after.netTotal, "netTotal is incorrect"),
        )
        if (updateResult.modified) {
            // Save modified basket
            // We still want to fail the validation that the client can review the changed basket, before trying again
            basketRepository.save(basket)
        }
    }

    private fun refreshIfValidationFailedDueToRefreshRequired(errors: NonEmptyList<Invalid>, basket: Basket) {
        if (errors.list.filterIsInstance<Invalid.RefreshRequired>().isNotEmpty()) {
            refreshService.refreshBasketDataWithoutSaving(basket)
            basketRepository.save(basket)
        }
    }


}
