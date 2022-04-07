package secondary.repository.payment.document

import config.DatabaseConfig
import core.application.metric.Metric
import core.domain.basketdata.model.BasketId
import core.domain.payment.PaymentProcessRepository
import core.domain.payment.model.PaymentProcess
import core.domain.payment.model.PaymentProcessAggregate
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import secondary.repository.common.document.AbstractMongoRepository

class PaymentProcessMongoRepository(
    config: DatabaseConfig,
) : PaymentProcessRepository, AbstractMongoRepository<PaymentProcess>(PaymentProcess::class.java, PaymentProcessAggregate::class.java) {

    private val logger = KotlinLogging.logger {}
    override val collection = database.getCollection<PaymentProcess>(config.documentOriented.paymentProcessCollectionName)

    override fun findPaymentProcess(id: BasketId): PaymentProcess {
        Metric.read("Payment")
        return collection.findOne(PaymentProcessAggregate::id eq id) ?: PaymentProcessAggregate(id)
    }

    override fun save(paymentProcess: PaymentProcess) {
        logger.info { "Save payment process for id ${paymentProcess.getBasketId()}" }
        Metric.write("Payment")
        collection.save(paymentProcess)
    }


}
