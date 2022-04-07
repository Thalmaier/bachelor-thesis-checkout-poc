package secondary.repository.checkoutdata.document

import config.DatabaseConfig
import core.application.metric.Metric
import core.domain.basketdata.model.BasketId
import core.domain.checkoutdata.CheckoutDataFactory
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.checkoutdata.model.CheckoutData
import core.domain.checkoutdata.model.CheckoutDataAggregate
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.repository.common.document.AbstractMongoRepository

class CheckoutDataMongoRepository(
    config: DatabaseConfig,
    private val checkoutDataFactory: CheckoutDataFactory,
) : CheckoutDataRepository, AbstractMongoRepository<CheckoutData>(CheckoutData::class.java, CheckoutDataAggregate::class.java) {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<CheckoutData>(config.documentOriented.checkoutDataCollectionName)

    override fun findCheckoutData(id: BasketId): CheckoutData {
        Metric.read("Checkout")
        return collection.findOne(CheckoutDataAggregate::id eq id)
            ?: checkoutDataFactory.createNewCheckoutData(id)
    }

    override fun save(checkoutData: CheckoutData) {
        logger.info { "Save payment process for id ${checkoutData.getBasketId()}" }
        Metric.write("Checkout")
        collection.save(checkoutData)
    }


}
