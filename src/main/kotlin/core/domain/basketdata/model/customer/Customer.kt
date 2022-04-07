package core.domain.basketdata.model.customer

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import core.domain.basketdata.model.BasketData
import core.domain.validation.model.ValidationResult

/**
 * Contains all necessary customer data for a [BasketData]
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(name = CustomerType.identifiedCustomerTypeName, value = IdentifiedCustomer::class),
    JsonSubTypes.Type(name = CustomerType.sessionIdCustomerTypeName, value = SessionCustomer::class),
])
sealed class Customer(val type: CustomerType) {

    abstract fun validate(parent: String, result: ValidationResult)

}