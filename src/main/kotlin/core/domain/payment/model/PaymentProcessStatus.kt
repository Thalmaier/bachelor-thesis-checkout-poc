package core.domain.payment.model

/**
 * Represents the status of a [PaymentProcess]
 */
enum class PaymentProcessStatus {
    TO_PAY,
    PARTIALLY_PAID,
    PAID
}
