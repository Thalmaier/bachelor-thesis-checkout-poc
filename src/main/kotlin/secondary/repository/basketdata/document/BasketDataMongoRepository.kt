package secondary.repository.basketdata.document

import config.DatabaseConfig
import core.application.metric.Metric
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketDataAggregate
import core.domain.basketdata.model.BasketId
import core.domain.exception.ResourceNotFoundError
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.repository.Repository
import secondary.repository.common.document.AbstractMongoRepository

/**
 * Repository for a [BasketData]
 */
@Repository
class BasketDataMongoRepository(
    config: DatabaseConfig,
) : BasketDataRepository, AbstractMongoRepository<BasketData>(BasketData::class.java, BasketDataAggregate::class.java) {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<BasketData>(config.documentOriented.basketDataCollectionName)

    override fun findBasketData(id: BasketId): BasketData {
        logger.info { "Load basket $id from the database" }
        Metric.read("Basket")
        return collection.findOne(BasketDataAggregate::id eq id)
            ?: throw ResourceNotFoundError("basketData", id)
    }

    override fun save(basketData: BasketData) {
        logger.info { "Store ${basketData.getBasketId()} in the database" }
        Metric.write("Basket")
        collection.save(basketData)
    }


}
