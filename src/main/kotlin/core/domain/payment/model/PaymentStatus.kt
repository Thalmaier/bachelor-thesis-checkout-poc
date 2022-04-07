package core.domain.payment.model

/**
 * Represents the status of a [Payment]
 */
enum class PaymentStatus {
    SELECTED,
    INITIALIZED,
    EXECUTED,
    CANCELED
}
