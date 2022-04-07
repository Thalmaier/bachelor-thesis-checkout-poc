package core.domain.validation.service

import core.domain.basket.model.Basket
import core.domain.exception.ValidationError

/**
 * Service for validation of a [Basket]
 */
interface ValidationService {

    /**
     * Validates a [Basket] and if validation failed throws an exception
     * @throws [ValidationError] if validation failed
     */
    fun validateAndThrowIfInvalid(basket: Basket)
}