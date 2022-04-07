package core.domain.basketdata.model.customer

/**
 * Necessary for the de- and serialization of a [Customer] to [IdentifiedCustomer] or [SessionCustomer]
 */
enum class CustomerType(val text: String) {
    IDENTIFIED(CustomerType.identifiedCustomerTypeName),
    SESSION_ID(CustomerType.sessionIdCustomerTypeName);

    companion object {
        const val identifiedCustomerTypeName = "IDENTIFIED"
        const val sessionIdCustomerTypeName = "SESSION_ID"
    }
}