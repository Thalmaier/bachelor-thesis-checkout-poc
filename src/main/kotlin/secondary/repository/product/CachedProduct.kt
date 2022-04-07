package secondary.repository.product

import core.domain.product.model.Product
import core.domain.product.model.ProductId
import secondary.repository.common.Cacheable
import java.time.LocalDateTime

/**
 * Used for caching [Product]s
 */
class CachedProduct(
    val product: Product, insertedAt: LocalDateTime,
) : Cacheable<ProductId, Product>(id = product.id, payload = product, insertedAt = insertedAt)
