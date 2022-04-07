package secondary.repository.product.document

import config.CachingConfig
import config.DatabaseConfig
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import secondary.common.TimeUtils
import secondary.repository.Repository
import secondary.repository.common.document.CacheMongoRepository
import secondary.repository.product.CachedProduct
import secondary.repository.product.ProductCache

/**
 * Repository for caching [Product]s with a [CachedProduct]
 */
@Repository
class CachedProductMongoRepository(
    cachingConfig: CachingConfig,
    databaseConfig: DatabaseConfig,
    mapping: (Product) -> CachedProduct = { product -> CachedProduct(product, TimeUtils.dateTimeNow()) },
) : ProductCache, CacheMongoRepository<ProductId, Product>(
    cachingConfig,
    databaseConfig.documentOriented.productCacheCollectionName,
    mapping,
    "ProductCache"
)
