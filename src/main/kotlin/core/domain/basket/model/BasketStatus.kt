package core.domain.basket.model

/**
 * Represents the status of a [Basket]
 * Certain actions are forbidden depending on the [BasketStatus]
 */
enum class BasketStatus {
    OPEN,
    CANCELED,
    FROZEN,
    FINALIZED;
}