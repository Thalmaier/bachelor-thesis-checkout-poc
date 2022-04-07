package core.domain.basket.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an id of a [Basket].
 */
data class BasketId(override val id: UUID = UUID.randomUUID()) : Id()
