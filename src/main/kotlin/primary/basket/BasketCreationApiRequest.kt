package primary.basket

import core.domain.basket.model.Basket
import core.domain.basket.model.OutletId
import core.domain.basket.model.customer.Customer

/**
 * Data class for deserialization of the request body for creating a new [Basket] resource
 */
data class BasketCreationApiRequest(
    val outletId: OutletId,
    val customer: Customer?,
)