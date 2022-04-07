package core.domain.basketdata.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an id of a [BasketData].
 */
data class BasketId(override val id: UUID = UUID.randomUUID()) : Id()
