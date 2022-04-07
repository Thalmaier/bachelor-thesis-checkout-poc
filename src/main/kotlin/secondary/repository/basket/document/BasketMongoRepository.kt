package secondary.repository.basket.document

import config.DatabaseConfig
import core.application.metric.Metric
import core.domain.basket.BasketRepository
import core.domain.basket.model.Basket
import core.domain.basket.model.BasketAggregate
import core.domain.basket.model.BasketId
import core.domain.basket.service.BasketRefreshService
import core.domain.exception.ResourceNotFoundError
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.repository.Repository
import secondary.repository.common.document.AbstractMongoRepository

/**
 * Repository for a [Basket] aggregate
 */
@Repository
class BasketMongoRepository(
    config: DatabaseConfig,
    private val basketRefreshService: BasketRefreshService,
) : BasketRepository, AbstractMongoRepository<Basket>(Basket::class.java, BasketAggregate::class.java) {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<Basket>(config.documentOriented.basketCollectionName)

    override fun getStaleBasket(id: BasketId): Basket {
        logger.info { "Load stale basket $id from the database" }
        Metric.read("Basket")
        return collection.findOne(BasketAggregate::id eq id) ?: throw ResourceNotFoundError("basket", id)
    }

    override fun getRefreshedBasket(id: BasketId): Basket {
        val staleBasket = getStaleBasket(id)
        return refreshBasket(staleBasket)
    }

    override fun save(basket: Basket) {
        logger.info { "Store ${basket.getBasketId()} in the database" }
        Metric.write("Basket")
        collection.save(basket)
    }

    /**
     * Refreshes a stale basket
     */
    private fun refreshBasket(staleBasket: Basket): Basket {
        val updateResult = basketRefreshService.refreshBasketDataWithoutSaving(staleBasket)
        if (updateResult.modified) {
            save(staleBasket)
        }
        return staleBasket
    }


}
