package core.domain.product.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an id of a [Product]
 */
data class ProductId(override val id: UUID = UUID.randomUUID()) : Id()
