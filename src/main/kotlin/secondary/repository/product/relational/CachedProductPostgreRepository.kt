package secondary.repository.product.relational

import config.CachingConfig
import core.domain.common.Transaction
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.product.model.Vat
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import secondary.common.TimeUtils
import secondary.repository.Repository
import secondary.repository.common.Cacheable
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.insertOrUpdate
import secondary.repository.common.relational.metricSelect
import secondary.repository.product.CachedProduct
import secondary.repository.product.ProductCache
import java.math.BigDecimal


/**
 * Repository for caching [Product]s with a [CachedProduct]
 */
@Repository
class CachedProductPostgreRepository(
    private val cachingConfig: CachingConfig,
    private val mapping: (Product) -> CachedProduct = { product -> CachedProduct(product, TimeUtils.dateTimeNow()) },
) : ProductCache, PostgresRepository(tables = arrayOf(CachedProductTable)) {

    override fun put(entity: Cacheable<ProductId, Product>) {
        Transaction {
            CachedProductTable.insertOrUpdate(CachedProductTable.productId) {
                it[productId] = entity.payload.id.id
                it[productName] = entity.payload.name
                it[productVatSign] = entity.payload.vat.sign
                it[productVatRate] = entity.payload.vat.rate.toDouble()
                it[productUpdatedAt] = entity.payload.updatedAt.toKotlinLocalDateTime()
                it[insertedAt] = entity.insertedAt.toKotlinLocalDateTime()
            }
        }
    }

    override fun get(key: ProductId): Product? {
        return getCacheable(key)?.payload
    }

    private fun getCacheable(key: ProductId): CachedProduct? {
        var product: CachedProduct? = null
        Transaction {
            product = CachedProductTable.metricSelect { CachedProductTable.productId eq key.id }.singleOrNull()?.let { dao ->
                CachedProduct(
                    insertedAt = dao[CachedProductTable.insertedAt].toJavaLocalDateTime(),
                    product = Product(
                        id = ProductId(dao[CachedProductTable.productId]),
                        name = dao[CachedProductTable.productName],
                        vat = Vat(
                            rate = BigDecimal(dao[CachedProductTable.productVatRate]),
                            sign = dao[CachedProductTable.productVatSign]
                        ),
                        updatedAt = dao[CachedProductTable.productUpdatedAt].toJavaLocalDateTime()
                    )
                )
            }
        }
        return product
    }

    override fun getAndUpdateIfInvalid(key: ProductId, fallback: () -> Product): Product {
        val cachedEntity = getCacheable(key)
        return when (cachedEntity != null && isValid(cachedEntity)) {
            true -> cachedEntity.payload
            else -> fallback().also { this.put(mapping(it)) }
        }
    }

    override fun isValid(cachedEntity: Cacheable<ProductId, Product>): Boolean {
        return !TimeUtils.olderThan(cachedEntity.insertedAt, cachingConfig.defaultCachedTimeInSeconds)
    }

}
