package secondary.repository.basket.relational

import core.domain.basket.BasketRepository
import core.domain.basket.model.*
import core.domain.basket.model.customer.*
import core.domain.basket.service.BasketRefreshService
import core.domain.calculation.model.BasketCalculationResult
import core.domain.calculation.model.BasketItemCalculationResult
import core.domain.calculation.model.CalculationResult
import core.domain.calculation.model.VatAmount
import core.domain.common.Transaction
import core.domain.order.model.Order
import core.domain.order.model.OrderRef
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentProcess
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.product.model.Vat
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.insertOrUpdate
import secondary.repository.common.relational.metricSelect
import java.math.BigDecimal
import java.util.*


class BasketPostgreRepository(
    private val basketRefreshService: BasketRefreshService,
) : BasketRepository, PostgresRepository(
    tables = arrayOf(
        BasketVatAmountTable, BasketItemTotalCostVatTable, BasketItemItemCostVatTable,
        PaymentTable, BasketItemTable, BasketAggregateTable
    )
) {

    override fun save(basket: Basket) {
        Transaction {
            mapToBasketDAO(basket)
        }
    }

    override fun getRefreshedBasket(id: BasketId): Basket {
        return refreshBasket(getStaleBasket(id))
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

    override fun getStaleBasket(id: BasketId): Basket {
        var basket: Basket? = null
        Transaction {
            val basketDao = BasketAggregateTable
                .metricSelect { BasketAggregateTable.id eq id.id }.single()
            basket = BasketAggregate(
                id = BasketId(basketDao[BasketAggregateTable.id]),
                outletId = basketDao[BasketAggregateTable.outletId],
                status = basketDao[BasketAggregateTable.status],
                fulfillment = basketDao[BasketAggregateTable.fulfillment],
                order = basketDao[BasketAggregateTable.orderId]?.let { Order(OrderRef(it)) },
                customer = mapCustomer(basketDao),
                billingAddress = mapBillingAddress(basketDao),
                shippingAddress = mapShippingAddress(basketDao),
                calculationResult = mapCalculationResult(basketDao, id),
                items = mapItems(id),
                paymentProcess = mapPaymentProcess(basketDao)
            )
        }
        return basket ?: throw IllegalStateException("Get stale basket is null")
    }

    private fun mapToBasketDAO(basket: Basket) {
        BasketAggregateTable.insertOrUpdate(BasketAggregateTable.id) {
            it[id] = basket.getBasketId().id
            it[outletId] = basket.getOutletId()
            it[status] = basket.getStatus()
            it[fulfillment] = basket.getFulfillment()
            basket.getCustomer()?.let { customer ->
                when (customer.type) {
                    CustomerType.SESSION_ID -> {
                        customer as SessionCustomer
                        it[customerType] = customer.type
                        it[customerSessionId] = customer.sessionId.id
                    }
                    CustomerType.IDENTIFIED -> {
                        customer as IdentifiedCustomer
                        it[customerType] = customer.type
                        it[customerBusinessType] = customer.businessType
                        it[customerCompanyName] = customer.companyName
                        it[customerCompanyTaxId] = customer.companyTaxId
                        it[customerCustomerTaxId] = customer.customerTaxId
                        it[customerEmail] = customer.email
                        it[customerNameFirstname] = customer.name.firstName
                        it[customerNameLastname] = customer.name.lastName
                    }
                }
            }
            basket.getShippingAddress()?.let { shipping ->
                it[shippingAddressCountry] = shipping.country
                it[shippingAddressCity] = shipping.city
                it[shippingAddressZipCode] = shipping.zipCode
                it[shippingAddressStreet] = shipping.street
                it[shippingAddressHouseNumber] = shipping.houseNumber
            }
            basket.getBillingAddress()?.let { billing ->
                it[billingAddressCountry] = billing.country
                it[billingAddressCity] = billing.city
                it[billingAddressZipCode] = billing.zipCode
                it[billingAddressStreet] = billing.street
                it[billingAddressHouseNumber] = billing.houseNumber
            }
            basket.getCalculationResult().let { calc ->
                it[grandTotal] = calc.grandTotal
                it[netTotal] = calc.netTotal
                it[shippingCostTotal] = calc.shippingCostTotal
            }

            basket.getPaymentProcess().let { paymentProcess ->
                it[paymentProcessAmountPaid] = paymentProcess.getAmountPaid()
                it[paymentProcessAmountToPay] = paymentProcess.getAmountToPay()
                it[paymentProcessAmountToReturn] = paymentProcess.getAmountToReturn()
                it[paymentProcessStatus] = paymentProcess.getStatus()
                it[paymentProcessExternalPaymentRef] = paymentProcess.externalPaymentRef?.id

            }
            it[orderId] = basket.getOrder()?.orderRef?.id
        }

        basket.getCalculationResult().vatAmounts.values.forEach { vatAmount ->
            BasketVatAmountTable.insertOrUpdate(BasketVatAmountTable.parentId, BasketVatAmountTable.sign) {
                it[parentId] = basket.getBasketId().id
                it[sign] = vatAmount.sign
                it[rate] = vatAmount.rate.toDouble()
                it[amount] = vatAmount.amount
            }
        }

        basket.getItems().forEach { item ->
            BasketItemTable.insertOrUpdate(BasketItemTable.id) {
                it[basketId] = basket.getBasketId().id
                it[id] = item.id.id
                it[productId] = item.getProductId().id
                it[productName] = item.getProduct().name
                it[productVatRate] = item.getProduct().vat.rate.toDouble()
                it[productVatSign] = item.getProduct().vat.sign
                it[productUpdatedAt] = item.getProduct().updatedAt.toKotlinLocalDateTime()
                it[priceIdOutletId] = item.getPrice().id.outletId
                it[priceIdProductId] = item.getPrice().id.productId.id
                it[priceGrossAmount] = item.getPrice().grossAmount
                it[priceUpdatedAt] = item.getPrice().updatedAt.toKotlinLocalDateTime()
                it[shippingCost] = item.getShippingCost()
                it[itemCostGrossAmount] = item.getCalculationResult().itemCost.grossAmount
                it[itemCostNetAmount] = item.getCalculationResult().itemCost.netAmount
                it[calculationShippingCost] = item.getCalculationResult().shippingCost
                it[totalCostGrossAmount] = item.getCalculationResult().totalCost.grossAmount
                it[totalCostNetAmount] = item.getCalculationResult().totalCost.netAmount
            }
            item.getCalculationResult().totalCost.vatAmounts.values.forEach { vatAmount ->
                BasketItemTotalCostVatTable.insertOrUpdate(BasketItemTotalCostVatTable.parentId, BasketItemTotalCostVatTable.sign) {
                    it[parentId] = item.id.id
                    it[sign] = vatAmount.sign
                    it[rate] = vatAmount.rate.toDouble()
                    it[amount] = vatAmount.amount
                }
            }
            item.getCalculationResult().itemCost.vatAmounts.values.forEach { vatAmount ->
                BasketItemItemCostVatTable.insertOrUpdate(BasketItemItemCostVatTable.parentId, BasketItemItemCostVatTable.sign) {
                    it[parentId] = item.id.id
                    it[sign] = vatAmount.sign
                    it[rate] = vatAmount.rate.toDouble()
                    it[amount] = vatAmount.amount
                }
            }
        }

        basket.getPaymentProcess().payments.forEach { payment ->
            PaymentTable.insertOrUpdate(PaymentTable.id) {
                it[basketId] = basket.getBasketId().id
                it[id] = payment.id.id
                it[method] = payment.method
                it[amountSelected] = payment.amountSelected
                it[amountUsed] = payment.amountUsed
                it[amountOverpaid] = payment.amountOverpaid
                it[status] = payment.status
            }
        }
    }

    private fun mapPayments(id: BasketId): MutableSet<Payment> {
        val set = mutableSetOf<Payment>()
        PaymentTable.metricSelect { PaymentTable.basketId eq id.id }.forEach { res ->
            set.add(
                Payment(
                    id = PaymentId(res[PaymentTable.id]),
                    method = res[PaymentTable.method],
                    amountSelected = res[PaymentTable.amountSelected],
                    _amountOverpaid = res[PaymentTable.amountOverpaid],
                    _amountUsed = res[PaymentTable.amountUsed],
                    _status = res[PaymentTable.status]
                )
            )
        }
        return set
    }

    private fun mapPaymentProcess(dao: ResultRow): PaymentProcess {
        return PaymentProcess(
            basketId = BasketId(dao[BasketAggregateTable.id]),
            payments = mapPayments(BasketId(dao[BasketAggregateTable.id])),
            amountToPay = dao[BasketAggregateTable.paymentProcessAmountToPay],
            amountToReturn = dao[BasketAggregateTable.paymentProcessAmountToReturn],
            amountPaid = dao[BasketAggregateTable.paymentProcessAmountPaid],
            status = dao[BasketAggregateTable.paymentProcessStatus],
            _externalPaymentRef = dao[BasketAggregateTable.paymentProcessExternalPaymentRef]?.let { ExternalPaymentRef(it) }
        )
    }

    private fun mapItems(id: BasketId): MutableList<BasketItem> {
        val list = mutableListOf<BasketItem>()
        BasketItemTable.metricSelect { BasketItemTable.basketId eq id.id }.forEach { res ->
            list.add(
                BasketItem(
                    id = BasketItemId(res[BasketItemTable.id]),
                    product = Product(
                        id = ProductId(res[BasketItemTable.productId]),
                        name = res[BasketItemTable.productName],
                        vat = Vat(
                            sign = res[BasketItemTable.productVatSign],
                            rate = BigDecimal(res[BasketItemTable.productVatRate]),
                        ),
                        updatedAt = res[BasketItemTable.productUpdatedAt].toJavaLocalDateTime()
                    ),
                    price = Price(
                        id = PriceId(
                            outletId = res[BasketItemTable.priceIdOutletId],
                            productId = ProductId(res[BasketItemTable.priceIdProductId]),
                        ),
                        grossAmount = res[BasketItemTable.priceGrossAmount],
                        updatedAt = res[BasketItemTable.priceUpdatedAt].toJavaLocalDateTime(),
                    ),
                    shippingCost = res[BasketItemTable.shippingCost],
                    calculationResult = mapBasketItemCalculationResult(res, res[BasketItemTable.id])
                )
            )
        }
        return list
    }

    private fun mapBasketItemCalculationResult(res: ResultRow, id: UUID): BasketItemCalculationResult {
        return BasketItemCalculationResult(
            itemCost = CalculationResult(
                grossAmount = res[BasketItemTable.itemCostGrossAmount],
                netAmount = res[BasketItemTable.itemCostNetAmount],
                vatAmounts = mapVatAmounts(BasketItemItemCostVatTable, id),
            ),
            shippingCost = res[BasketItemTable.calculationShippingCost],
            totalCost = CalculationResult(
                grossAmount = res[BasketItemTable.totalCostGrossAmount],
                netAmount = res[BasketItemTable.totalCostNetAmount],
                vatAmounts = mapVatAmounts(BasketItemTotalCostVatTable, id),
            )
        )
    }

    private fun mapCalculationResult(dao: ResultRow, id: BasketId): BasketCalculationResult {
        return BasketCalculationResult(
            grandTotal = dao[BasketAggregateTable.grandTotal],
            netTotal = dao[BasketAggregateTable.netTotal],
            shippingCostTotal = dao[BasketAggregateTable.shippingCostTotal],
            vatAmounts = mapVatAmounts(BasketVatAmountTable, id.id),
        )
    }

    private fun <T : VatAmountTable> mapVatAmounts(table: T, id: UUID): Map<Int, VatAmount> {
        val map = mutableMapOf<Int, VatAmount>()
        table as Table
        table.metricSelect { table.parentId eq id }.forEach { res ->
            val vatAmount = VatAmount(
                sign = res[table.sign],
                rate = BigDecimal(res[table.rate]),
                amount = res[table.amount],
            )
            map[vatAmount.sign] = vatAmount
        }
        return map
    }


    private fun mapShippingAddress(dao: ResultRow): Address? {
        if (dao[BasketAggregateTable.shippingAddressCity] == null) {
            return null
        }
        return Address(
            country = dao[BasketAggregateTable.shippingAddressCountry]!!,
            city = dao[BasketAggregateTable.shippingAddressCity]!!,
            zipCode = dao[BasketAggregateTable.shippingAddressZipCode]!!,
            street = dao[BasketAggregateTable.shippingAddressStreet]!!,
            houseNumber = dao[BasketAggregateTable.shippingAddressHouseNumber]!!
        )
    }

    private fun mapBillingAddress(dao: ResultRow): Address? {
        if (dao[BasketAggregateTable.billingAddressCountry] == null) {
            return null
        }
        return Address(
            country = dao[BasketAggregateTable.billingAddressCountry]!!,
            city = dao[BasketAggregateTable.billingAddressCity]!!,
            zipCode = dao[BasketAggregateTable.billingAddressZipCode]!!,
            street = dao[BasketAggregateTable.billingAddressStreet]!!,
            houseNumber = dao[BasketAggregateTable.billingAddressHouseNumber]!!
        )
    }

    private fun mapCustomer(dao: ResultRow): Customer? {
        return when (dao[BasketAggregateTable.customerType]) {
            CustomerType.SESSION_ID -> SessionCustomer(
                sessionId = SessionId(dao[BasketAggregateTable.customerSessionId]!!)
            )
            CustomerType.IDENTIFIED -> IdentifiedCustomer(
                companyName = dao[BasketAggregateTable.customerCompanyName]!!,
                companyTaxId = dao[BasketAggregateTable.customerCompanyTaxId]!!,
                customerTaxId = dao[BasketAggregateTable.customerCustomerTaxId]!!,
                email = dao[BasketAggregateTable.customerEmail]!!,
                name = CustomerName(
                    firstName = dao[BasketAggregateTable.customerNameFirstname]!!,
                    lastName = dao[BasketAggregateTable.customerNameLastname]!!
                ),
                businessType = dao[BasketAggregateTable.customerBusinessType]!!
            )
            else -> null
        }
    }

}
