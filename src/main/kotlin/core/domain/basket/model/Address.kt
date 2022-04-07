package core.domain.basket.model

import core.domain.validation.invalidIfBlank
import core.domain.validation.model.ValidationResult

/**
 * Value object for an address.
 */
data class Address(
    val country: String,
    val city: String,
    val zipCode: String,
    val street: String,
    val houseNumber: String,
) {

    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.address"
        result.addResults(
            country.invalidIfBlank(node, "country"),
            city.invalidIfBlank(node, "city"),
            zipCode.invalidIfBlank(node, "zipCode"),
            street.invalidIfBlank(node, "street"),
            houseNumber.invalidIfBlank(node, "houseNumber")
        )
    }

}
