package core.domain.validation.service

import core.domain.basketdata.model.BasketData
import core.domain.exception.ValidationError
import core.domain.payment.model.PaymentProcess

/**
 * Service for validation of a [BasketData]
 */
interface ValidationService {

    /**
     * Validates all aggregates
     * @throws [ValidationError] if validation failed
     */
    fun validateAndThrowIfInvalid(basketData: BasketData, paymentProcess: PaymentProcess)
}