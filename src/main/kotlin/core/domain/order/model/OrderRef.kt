package core.domain.order.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an reference of an external [Order].
 */
data class OrderRef(override val id: UUID = UUID.randomUUID()) : Id()