package config

import com.mongodb.client.MongoClient
import core.application.aggregate.AggregateApplicationService
import core.application.aggregate.AggregationApiPort
import core.application.basketdata.BasketDataApiPort
import core.application.basketdata.BasketDataApplicationService
import core.application.basketdata.BasketDataItemApiPort
import core.application.basketdata.BasketDataItemApplicationService
import core.application.calculation.BasketCalculationApiPort
import core.application.calculation.BasketCalculationApplicationService
import core.application.checkoutdata.CheckoutDataApiPort
import core.application.checkoutdata.CheckoutDataApplicationService
import core.application.metric.InMemoryMetricOperator
import core.application.metric.MetricOperator
import core.application.metric.NoOpsMetricOperator
import core.application.pricing.PricingApplicationService
import core.application.pricing.PricingService
import core.application.product.ProductApplicationService
import core.application.product.ProductService
import core.domain.basketdata.BasketDataFactory
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.service.BasketDataRefreshDomainService
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.calculation.BasketCalculationRepository
import core.domain.calculation.service.BasketCalculationDomainService
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.CheckoutDataFactory
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.order.OrderPort
import core.domain.order.service.OrderDomainService
import core.domain.order.service.OrderService
import core.domain.payment.PaymentPort
import core.domain.payment.PaymentProcessRepository
import core.domain.payment.service.PaymentProcessApiPort
import core.domain.payment.service.PaymentProcessDomainService
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
import secondary.repository.basketdata.document.BasketDataMongoRepository
import secondary.repository.basketdata.relational.BasketDataPostgreRepository
import secondary.repository.calculation.document.BasketCalculationMongoRepository
import secondary.repository.calculation.relational.BasketCalculationPostgreRepository
import secondary.repository.checkoutdata.document.CheckoutDataMongoRepository
import secondary.repository.checkoutdata.relational.CheckoutDataPostgreRepository
import secondary.repository.common.InMemoryDatabaseProcess
import secondary.repository.common.document.DefaultMongoClient
import secondary.repository.common.document.InMemoryMongoClient
import secondary.repository.common.relational.DefaultPostgresClient
import secondary.repository.common.relational.InMemoryPostgresClient
import secondary.repository.common.relational.PostgresClient
import secondary.repository.payment.document.PaymentProcessMongoRepository
import secondary.repository.payment.relational.PaymentProcessPostgreRepository
import secondary.repository.price.PriceCache
import secondary.repository.price.document.CachedPriceMongoRepository
import secondary.repository.price.relational.CachedPricePostgreRepository
import secondary.repository.product.ProductCache
import secondary.repository.product.document.CachedProductMongoRepository
import secondary.repository.product.relational.CachedProductPostgreRepository

/**
 * Returns all koin modules needed for dependency injection
 */
fun defaultKoinModules(config: Configuration = loadDefaultConfig()): Module = module {
    // Config
    single { config }
    single { config.application }
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
        single<PaymentProcessRepository> { PaymentProcessPostgreRepository() }
        single<BasketCalculationRepository> { BasketCalculationPostgreRepository() }
        single<CheckoutDataRepository> { CheckoutDataPostgreRepository() }
        single<BasketDataRepository> { BasketDataPostgreRepository() }
        single<PriceCache> { CachedPricePostgreRepository(get()) }
        single<ProductCache> { CachedProductPostgreRepository(get()) }

        if (InMemoryDatabaseProcess.useInMemoryPostgreDB(config.database)) {
            single<PostgresClient> { InMemoryPostgresClient(get()) }
        } else {
            single<PostgresClient> { DefaultPostgresClient(get()) }
        }

    } else {
        single<PaymentProcessRepository> { PaymentProcessMongoRepository(get()) }
        single<BasketCalculationRepository> { BasketCalculationMongoRepository(get()) }
        single<CheckoutDataRepository> { CheckoutDataMongoRepository(get(), get()) }
        single<BasketDataRepository> { BasketDataMongoRepository(get()) }

        if (InMemoryDatabaseProcess.usesInMemoryMongoDB(config.database)) {
            single<MongoClient> { InMemoryMongoClient(get()) }
        } else {
            single<MongoClient> { DefaultMongoClient(get()) }
        }

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
    single { BasketDataFactory() }
    single { CheckoutDataFactory() }
    // Application Service
    single<ProductService> { ProductApplicationService(get()) }
    single<PricingService> { PricingApplicationService(get()) }
    single<ValidationService> { ValidationDomainService(get(), get(), get(), get(), get(), get()) }
    // Domain Service
    single<ShippingCostService> { ShippingCostDomainService(get()) }
    single<BasketDataRefreshService> { BasketDataRefreshDomainService(get(), get(), get(), get(), get(), get()) }
    single<OrderService> { OrderDomainService(get(), get()) }
    single<BasketCalculationService> { BasketCalculationDomainService(get(), get()) }

    // Primary Ports
    single<AggregationApiPort> { AggregateApplicationService(get(), get(), get(), get()) }
    single<BasketDataApiPort> { BasketDataApplicationService(get(), get(), get(), get(), get(), get()) }
    single<BasketDataItemApiPort> { BasketDataItemApplicationService(get(), get(), get(), get(), get(), get(), get()) }
    single<PaymentProcessApiPort> { PaymentProcessDomainService(get(), get(), get(), get(), get(), get(), get(), get()) }
    single<BasketCalculationApiPort> { BasketCalculationApplicationService(get()) }
    single<CheckoutDataApiPort> { CheckoutDataApplicationService(get(), get(), get(), get(), get(), get()) }
}
