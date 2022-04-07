package config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import config.Config.invoke
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import javax.money.CurrencyUnit
import javax.money.Monetary


/**
 * Object containing the current [Configuration]. [invoke] returns the application [Configuration]
 */
object Config : KoinComponent {
    private val config by inject<Configuration>()

    /**
     * Returns the current [Configuration] of the application
     */
    operator fun invoke(): Configuration = config
}

/**
 * Loads the default application configuration from the resource file 'application-config.yaml'
 */
fun loadDefaultConfig(): Configuration = ConfigLoader.Builder()
    .addSource(PropertySource.file(File("application-config.yaml"), true))
    .addSource(PropertySource.resource("/application-config.yaml"))
    .build().loadConfigOrThrow()

/**
 * Represents the possible configuration of the application.
 */
data class Configuration(
    val application: ApplicationConfig,
    val database: DatabaseConfig,
    val businessRules: BusinessRules,
    val currency: CurrencyConfig,
    val customer: CustomerConfig,
    val caching: CachingConfig,
    val price: PriceConfig,
    val product: ProductConfig,
)

data class ApplicationConfig(val port: Int, val simulateApiCalls: Boolean, val recordMetrics: Boolean)

data class DatabaseConfig(
    val useRelationalDatabase: Boolean,
    val documentOriented: DocumentOrientedDatabaseConfig,
    val relational: RelationalDatabaseConfig,
)

data class DocumentOrientedDatabaseConfig(
    val databaseName: String, val connectionUrl: String,
    val basketDataCollectionName: String, val paymentProcessCollectionName: String,
    val basketCalculationCollectionName: String, val checkoutDataCollectionName: String,
    val priceCacheCollectionName: String, val productCacheCollectionName: String,
    val useInMemoryMongodb: Boolean, val inMemoryMongodbPort: Int,
)

data class RelationalDatabaseConfig(
    val connectionUrl: String,
    val driverClassName: String,
    val username: String,
    val password: String,
    val useInMemoryPostgres: Boolean,
    val inMemoryPostgresPort: Int,
)

data class BusinessRules(val maxItemAmount: Int, val maxSameItemCount: Int)

data class CurrencyConfig(private val unit: String, val currencyUnit: CurrencyUnit = Monetary.getCurrency(unit))

data class CustomerConfig(val emailRegex: String, val taxIdRegex: String)

data class CachingConfig(val defaultCachedTimeInSeconds: Long)

data class PriceConfig(val updatePriceAfterSeconds: Long)

data class ProductConfig(val updateProductAfterSeconds: Long)
