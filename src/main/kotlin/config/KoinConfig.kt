package config

import com.mongodb.client.MongoClient
import core.application.basket.BasketApiPort
import core.application.basket.BasketApplicationService
import core.application.basket.BasketItemApiPort
import core.application.basket.BasketItemApplicationService
import core.application.metric.InMemoryMetricOperator
import core.application.metric.MetricOperator
import core.application.metric.NoOpsMetricOperator
import core.application.pricing.PricingApplicationService
import core.application.pricing.PricingService
import core.application.product.ProductApplicationService
import core.application.product.ProductService
import core.domain.basket.BasketFactory
import core.domain.basket.BasketRepository
import core.domain.basket.FulfillmentPort
import core.domain.basket.service.BasketRefreshDomainService
import core.domain.basket.service.BasketRefreshService
import core.domain.order.OrderPort
import core.domain.order.service.OrderDomainService
import core.domain.order.service.OrderService
import core.domain.payment.PaymentPort
import core.domain.payment.service.PaymentApiPort
import core.domain.payment.service.PaymentDomainService
import core.domain.price.PricePort
import core.domain.product.ProductPort
import core.domain.shipping.ShippingPort
import core.domain.shipping.service.ShippingCostDomainService
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.service.ValidationDomainService
import core.domain.validation.service.ValidationService
import org.koin.core.module.Module
import org.koin.dsl.module
import secondary.adapter.fulfillment.FulfillmentAdapter
import secondary.adapter.fulfillment.FulfillmentApiService
import secondary.adapter.order.OrderAdapter
import secondary.adapter.order.OrderApiService
import secondary.adapter.payment.PaymentAdapter
import secondary.adapter.payment.PaymentApiService
import secondary.adapter.price.CachedPriceAdapter
import secondary.adapter.price.PriceAdapter
import secondary.adapter.price.PriceApiService
import secondary.adapter.product.CachedProductAdapter
import secondary.adapter.product.ProductAdapter
import secondary.adapter.product.ProductApiService
import secondary.adapter.shipping.ShippingAdapter
import secondary.adapter.shipping.ShippingApiService
import secondary.repository.basket.document.BasketMongoRepository
import secondary.repository.basket.relational.BasketPostgreRepository
import secondary.repository.common.InMemoryDatabaseProcess
import secondary.repository.common.document.DefaultMongoClient
import secondary.repository.common.document.InMemoryMongoClient
import secondary.repository.common.relational.DefaultPostgresClient
import secondary.repository.common.relational.InMemoryPostgresClient
import secondary.repository.common.relational.PostgresClient
import secondary.repository.price.PriceCache
import secondary.repository.price.document.CachedPriceMongoRepository
import secondary.repository.price.relational.CachedPricePostgreRepository
import secondary.repository.product.ProductCache
import secondary.repository.product.document.CachedProductMongoRepository
import secondary.repository.product.relational.CachedProductPostgreRepository

/**
 * Returns configuration koin modules needed for dependency injection
 */
fun defaultKoinModules(config: Configuration = loadDefaultConfig()): Module = module {
    // Config
    single { config }
    single { config.database }
    single { config.caching }
    single { config.database.documentOriented }
    single { config.database.relational }

    if (config.application.recordMetrics) {
        single<MetricOperator> { InMemoryMetricOperator() }
    } else {
        single<MetricOperator> { NoOpsMetricOperator() }
    }

    // Secondary Ports
    // Repositories
    if (config.database.useRelationalDatabase) {
        // Relational database implementation
        single<BasketRepository> { BasketPostgreRepository(get()) }
        single<PriceCache> { CachedPricePostgreRepository(get()) }
        single<ProductCache> { CachedProductPostgreRepository(get()) }

        if (InMemoryDatabaseProcess.useInMemoryPostgreDB(config.database)) {
            single<PostgresClient> { InMemoryPostgresClient(get()) }
        } else {
            single<PostgresClient> { DefaultPostgresClient(get()) }
        }

    } else {

        if (InMemoryDatabaseProcess.usesInMemoryMongoDB(config.database)) {
            single<MongoClient> { InMemoryMongoClient(get()) }
        } else {
            single<MongoClient> { DefaultMongoClient(get()) }
        }

        // Document Orientated database implementation
        single<BasketRepository> { BasketMongoRepository(get(), get()) }
        single<PriceCache> { CachedPriceMongoRepository(get(), get()) }
        single<ProductCache> { CachedProductMongoRepository(get(), get()) }

    }

    // Adapter
    single { ProductApiService() }
    single { ProductAdapter(get()) }
    single<ProductPort> { CachedProductAdapter(get(), get()) }
    single { PriceApiService() }
    single { PriceAdapter(get()) }
    single<PricePort> { CachedPriceAdapter(get(), get()) }
    single { ShippingApiService() }
    single<ShippingPort> { ShippingAdapter(get()) }
    single { PaymentApiService() }
    single<PaymentPort> { PaymentAdapter(get()) }
    single { FulfillmentApiService() }
    single<FulfillmentPort> { FulfillmentAdapter(get()) }
    single { OrderApiService() }
    single<OrderPort> { OrderAdapter(get()) }

    // Domain
    single { BasketFactory() }
    // Application Service
    single<ProductService> { ProductApplicationService(get()) }
    single<PricingService> { PricingApplicationService(get()) }
    single<ValidationService> { ValidationDomainService(get(), get(), get()) }
    // Domain Service
    single<ShippingCostService> { ShippingCostDomainService(get()) }
    single<BasketRefreshService> { BasketRefreshDomainService(get(), get(), get()) }
    single<OrderService> { OrderDomainService(get(), get()) }

    // Primary Ports
    single<BasketApiPort> { BasketApplicationService(get(), get(), get(), get()) }
    single<BasketItemApiPort> { BasketItemApplicationService(get(), get(), get(), get()) }
    single<PaymentApiPort> { PaymentDomainService(get(), get(), get(), get()) }
}
