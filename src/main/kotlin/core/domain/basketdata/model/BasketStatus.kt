package core.domain.basketdata.model

/**
 * Represents the status of a [BasketData]
 * Certain actions are forbidden depending on the [BasketStatus]
 */
enum class BasketStatus {
    OPEN,
    CANCELED,
    FROZEN,
    FINALIZED;
}