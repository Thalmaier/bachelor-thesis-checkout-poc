import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import config.Configuration
import config.defaultKoinModules
import core.application.aggregate.AggregationApiPort
import core.application.basketdata.BasketDataApiPort
import core.application.basketdata.BasketDataItemApiPort
import core.application.calculation.BasketCalculationApiPort
import core.application.checkoutdata.CheckoutDataApiPort
import core.domain.payment.service.PaymentProcessApiPort
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.litote.kmongo.util.KMongoConfiguration
import org.zalando.jackson.datatype.money.MoneyModule
import primary.aggregate.AggregateController
import primary.basketdata.BasketDataController
import primary.calculation.BasketCalculationController
import primary.checkoutdata.CheckoutDataController
import primary.common.apiExceptionHandler
import primary.common.contentHandler
import primary.metric.MetricController
import primary.payment.PaymentController
import secondary.repository.common.InMemoryDatabaseProcess
import secondary.repository.common.document.MongoDB
import secondary.repository.common.relational.PostgresDB

fun main() {
    CheckoutApplication().execute()
}

/**
 * Main starting point of the application
 */
class CheckoutApplication : KoinComponent {

    private val logger = KotlinLogging.logger {}
    private val config: Configuration by inject()
    private val aggregationApplicationService: AggregationApiPort by inject()
    private val basketDataApiPort: BasketDataApiPort by inject()
    private val basketDataItemApiPort: BasketDataItemApiPort by inject()
    private val paymentApiPort: PaymentProcessApiPort by inject()
    private val basketCalculationApiPort: BasketCalculationApiPort by inject()
    private val checkoutDataApiPort: CheckoutDataApiPort by inject()

    /**
     * Initializes all necessary configuration of the application
     */
    init {
        initKMongoConfiguration()
        startKoin()
    }

    /**
     * Starting function of this application
     */
    fun execute() {
        InMemoryDatabaseProcess.startInMemoryDatabaseIfNecessary(config.database)
        val server = embeddedServer(Netty, port = config.application.port) {
            defaultServerModules()
        }
        shutdownHook(server)
        logger.info { "Starting server \uD83D\uDE80" }
        server.start(true)
    }

    /**
     * Default modules used by the web server of this application to respond to api requests
     */
    val defaultServerModules: Application.() -> Unit = {
        contentHandler()
        apiExceptionHandler()
        routing {
            AggregateController(aggregationApplicationService).route(this)
            BasketDataController(basketDataApiPort, basketDataItemApiPort).route(this)
            PaymentController(paymentApiPort).route(this)
            CheckoutDataController(checkoutDataApiPort).route(this)
            BasketCalculationController(basketCalculationApiPort).route(this)
            MetricController().route(this)
        }
    }

    /**
     * Initializes the database configuration
     */
    private fun initKMongoConfiguration() {
        KMongoConfiguration.registerBsonModule(MoneyModule())
        KMongoConfiguration.bsonMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    /**
     * Start koin for dependency injection
     */
    private fun startKoin() {
        logger.info { "Starting koin \uD83E\uDE99" }
        startKoin {
            modules(
                defaultKoinModules()
            )
        }
    }

    /**
     * Add shutdown hook to the jvm to make sure the database and server is shutdown too
     */
    private fun shutdownHook(server: NettyApplicationEngine) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                logger.info { "Shutting down database and server gracefully... ⏲️" }

                if (config.database.useRelationalDatabase) {
                    PostgresDB.close()
                } else {
                    MongoDB.close()
                }

                InMemoryDatabaseProcess.close()

                server.stop(1000L, 1000L)
            }
        })
    }

}
