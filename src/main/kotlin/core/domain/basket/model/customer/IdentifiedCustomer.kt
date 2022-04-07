package core.domain.basket.model.customer

import com.fasterxml.jackson.annotation.JsonIgnore
import config.Config
import core.domain.validation.invalidIfBlank
import core.domain.validation.model.ValidationResult
import core.domain.validation.validIfMatch

/**
 * Containing all data of a logged in [Customer]
 */
data class IdentifiedCustomer(
    val companyName: String = "",
    val companyTaxId: String = "",
    val customerTaxId: String = "",
    val email: Email = "",
    val name: CustomerName = CustomerName(),
    val businessType: BusinessType = BusinessType.B2C,
) : Customer(CustomerType.IDENTIFIED) {

    @JsonIgnore
    private val TAX_ID_FORMAT = Config().customer.taxIdRegex.toRegex()

    override fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.customer"
        when (businessType) {
            BusinessType.B2B -> {
                result.addResults(companyName.invalidIfBlank(node, "companyName"))
                result.addResults(validIfMatch(node, "companyTaxId", companyTaxId, TAX_ID_FORMAT))
            }
            BusinessType.B2C -> {
                name.validate(node, result)
                email.validate(node, result)
                if (customerTaxId.isNotBlank()) {
                    result.addResults(validIfMatch(node, "customerTaxId", customerTaxId, TAX_ID_FORMAT))
                }
            }
        }
    }

}


