package primary.basketdata

import core.domain.basketdata.model.OutletId
import core.domain.basketdata.model.customer.Customer

/**
 * Data class for deserialization of the request body for creating a new basket resource
 */
data class BasketCreationApiRequest(
    val outletId: OutletId,
    val customer: Customer?,
)