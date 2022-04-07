package core.domain.basketdata.model.customer

import core.domain.common.Id
import java.util.*

/**
 * Value object for an id of a [SessionCustomer].
 */
data class SessionId(override val id: UUID = UUID.randomUUID()) : Id()