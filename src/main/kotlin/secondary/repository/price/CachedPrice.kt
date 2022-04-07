package secondary.repository.price

import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.repository.common.Cacheable
import java.time.LocalDateTime

/**
 * Used for caching [Price]s
 */
class CachedPrice(val price: Price, insertedAt: LocalDateTime) : Cacheable<PriceId, Price>(price.id, price, insertedAt)
