package secondary.repository.common.document

import config.CachingConfig
import core.application.metric.Metric
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.common.TimeUtils
import secondary.repository.common.Cache
import secondary.repository.common.Cacheable

/**
 * Class for combining a [Cache] and a [MongoRepository].
 * Extend this class to use it for a concrete [Cacheable].
 */
abstract class CacheMongoRepository<K, E>(
    private val config: CachingConfig,
    private val collectionName: String,
    private val mapping: (E) -> Cacheable<K, E>,
    private val className: String,
) : Cache<K, E>, MongoRepository<Cacheable<K, E>> {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<Cacheable<K, E>>(collectionName)

    override fun put(entity: Cacheable<K, E>) {
        logger.info { "Store entity $entity in cache collection $collectionName" }
        Metric.write(className)
        collection.save(entity)
    }

    override fun get(key: K): E? {
        return getCacheable(key)?.payload
    }

    private fun getCacheable(key: K): Cacheable<K, E>? {
        logger.info { "Get cached entity with key $key from collection $collectionName" }
        Metric.read(className)
        return collection.findOne(Cacheable<K, E>::id eq key)
    }

    override fun getAndUpdateIfInvalid(key: K, fallback: () -> E): E {
        val cachedEntity = getCacheable(key)
        return when (cachedEntity != null && isValid(cachedEntity)) {
            true -> cachedEntity.payload
            else -> {
                logger.info("Loaded cached entity $key is not valid anymore. Use fallback method.")
                fallback().also { this.put(mapping(it)) }
            }
        }
    }

    override fun isValid(cachedEntity: Cacheable<K, E>): Boolean {
        return !TimeUtils.olderThan(cachedEntity.insertedAt, config.defaultCachedTimeInSeconds)
    }

}
