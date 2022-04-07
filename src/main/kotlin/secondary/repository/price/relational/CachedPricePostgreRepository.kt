package secondary.repository.price.relational

import config.CachingConfig
import core.domain.common.Transaction
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import core.domain.product.model.ProductId
import core.domain.shipping.service.ShippingCostService
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import secondary.common.TimeUtils
import secondary.repository.Repository
import secondary.repository.common.Cacheable
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.insertOrUpdate
import secondary.repository.common.relational.metricSelect
import secondary.repository.price.CachedPrice
import secondary.repository.price.CachedPriceTable
import secondary.repository.price.PriceCache

/**
 * Repository for caching [Price]s with a [CachedPrice]
 */
@Repository
class CachedPricePostgreRepository(
    private val cachingConfig: CachingConfig,
    private val mapping: (Price) -> CachedPrice = { price -> CachedPrice(price, TimeUtils.dateTimeNow()) },
) : PriceCache, PostgresRepository(tables = arrayOf(CachedPriceTable)) {

    override fun put(entity: Cacheable<PriceId, Price>) {
        Transaction {
            CachedPriceTable.insertOrUpdate(CachedPriceTable.productId, CachedPriceTable.outletId) {
                it[productId] = entity.id.productId.id
                it[outletId] = entity.id.outletId
                it[grossAmount] = entity.payload.grossAmount.with(ShippingCostService.getRoundingOptions())
                it[productUpdatedAt] = entity.payload.updatedAt.toKotlinLocalDateTime()
                it[insertedAt] = entity.insertedAt.toKotlinLocalDateTime()
            }
        }
    }

    override fun get(key: PriceId): Price? {
        return getCacheable(key)?.payload
    }

    private fun getCacheable(key: PriceId): CachedPrice? {
        var price: CachedPrice? = null
        Transaction {
            price = CachedPriceTable.metricSelect {
                (CachedPriceTable.productId eq key.productId.id) and (CachedPriceTable.outletId eq key.outletId)
            }.singleOrNull()?.let { dao ->
                CachedPrice(
                    insertedAt = dao[CachedPriceTable.insertedAt].toJavaLocalDateTime(),
                    price = Price(
                        id = PriceId(dao[CachedPriceTable.outletId], ProductId(dao[CachedPriceTable.productId])),
                        grossAmount = dao[CachedPriceTable.grossAmount],
                        updatedAt = dao[CachedPriceTable.productUpdatedAt].toJavaLocalDateTime()
                    )
                )
            }
        }
        return price
    }

    override fun getAndUpdateIfInvalid(key: PriceId, fallback: () -> Price): Price {
        val cachedEntity = getCacheable(key)
        return when (cachedEntity != null && isValid(cachedEntity)) {
            true -> cachedEntity.payload
            else -> fallback().also { this.put(mapping(it)) }
        }
    }

    override fun isValid(cachedEntity: Cacheable<PriceId, Price>): Boolean {
        return !TimeUtils.olderThan(cachedEntity.insertedAt, cachingConfig.defaultCachedTimeInSeconds)
    }

}
