package secondary.repository.calculation.document

import config.DatabaseConfig
import core.application.metric.Metric
import core.domain.basketdata.model.BasketId
import core.domain.calculation.BasketCalculationRepository
import core.domain.calculation.model.BasketCalculation
import core.domain.calculation.model.BasketCalculationAggregate
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.repository.common.document.AbstractMongoRepository

class BasketCalculationMongoRepository(
    config: DatabaseConfig,
) : BasketCalculationRepository,
    AbstractMongoRepository<BasketCalculation>(BasketCalculation::class.java, BasketCalculationAggregate::class.java) {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<BasketCalculation>(config.documentOriented.basketCalculationCollectionName)

    override fun findBasketCalculation(id: BasketId): BasketCalculation {
        Metric.read("Calculation")
        return collection.findOne(BasketCalculationAggregate::id eq id) ?: BasketCalculationAggregate(id)
    }

    override fun save(basketCalculation: BasketCalculation) {
        logger.info { "Save payment process for id ${basketCalculation.getBasketId()}" }
        Metric.write("Calculation")
        collection.save(basketCalculation)
    }


}
