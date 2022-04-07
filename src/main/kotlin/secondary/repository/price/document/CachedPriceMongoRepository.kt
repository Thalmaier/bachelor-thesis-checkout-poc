package secondary.repository.price.document

import config.CachingConfig
import config.DatabaseConfig
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.common.TimeUtils
import secondary.repository.Repository
import secondary.repository.common.document.CacheMongoRepository
import secondary.repository.price.CachedPrice
import secondary.repository.price.PriceCache

/**
 * Repository for caching [Price]s with [CachedPrice]
 */
@Repository
class CachedPriceMongoRepository(
    cachingConfig: CachingConfig,
    databaseConfig: DatabaseConfig,
    mapping: (Price) -> CachedPrice = { price -> CachedPrice(price, TimeUtils.dateTimeNow()) },
) : PriceCache, CacheMongoRepository<PriceId, Price>(
    cachingConfig,
    databaseConfig.documentOriented.priceCacheCollectionName,
    mapping,
    "PriceCache"
)
