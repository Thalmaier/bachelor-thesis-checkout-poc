package core.domain.basketdata.model.customer

import core.domain.validation.model.ValidationResult

/**
 * A [Customer] that is not logged in. Identified by a [SessionId]
 */
data class SessionCustomer(
    val sessionId: SessionId,
) : Customer(CustomerType.SESSION_ID) {
    override fun validate(parent: String, result: ValidationResult) {
        return
    }
}