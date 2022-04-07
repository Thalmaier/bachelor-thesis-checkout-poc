package core.domain.exception

import core.domain.validation.model.Invalid

/**
 * Thrown if a validation failed
 */
class ValidationError(val errors: List<Invalid>) : RecognizedDomainError(
    message = errors.joinToString(", ") { error -> error.message }
)
