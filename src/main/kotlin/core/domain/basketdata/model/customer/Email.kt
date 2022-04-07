package core.domain.basketdata.model.customer

import config.Config
import core.domain.validation.model.ValidationResult
import core.domain.validation.validIfMatch

/**
 * Email of a [IdentifiedCustomer]
 */
typealias Email = String

private val emailRegex: Regex = Config().customer.emailRegex.toRegex()

fun Email.validate(parent: String, result: ValidationResult) {
    result.addResults(validIfMatch(parent, "email", this, emailRegex))
}

