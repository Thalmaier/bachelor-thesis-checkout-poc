package core.domain.basketdata.model.customer

import core.domain.validation.invalidIfBlank
import core.domain.validation.model.ValidationResult

/**
 * Containing [firstName] and [lastName] of a [IdentifiedCustomer]
 */
data class CustomerName(
    val firstName: String = "",
    val lastName: String = "",
) {

    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.name"
        result.addResults(
            firstName.invalidIfBlank(node, "firstname"),
            lastName.invalidIfBlank(node, "lastName")
        )
    }
}
