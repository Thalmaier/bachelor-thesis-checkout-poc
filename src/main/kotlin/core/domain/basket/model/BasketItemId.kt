package core.domain.basket.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an id of a [BasketItem].
 */
data class BasketItemId(override val id: UUID = UUID.randomUUID()) : Id()
