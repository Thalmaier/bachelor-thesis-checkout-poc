package secondary.repository.basket.relational

import core.domain.basket.model.BasketStatus
import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.customer.BusinessType
import core.domain.basket.model.customer.CustomerType
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcessStatus
import core.domain.payment.model.PaymentStatus
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.CompositeColumn
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import secondary.repository.common.relational.money
import java.util.*
import javax.money.MonetaryAmount

object BasketAggregateTable : Table() {
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id, name = "PK_BasketAggregate")
    val outletId = varchar("outletId", 50)
    val status = enumeration("status", BasketStatus::class)
    val fulfillment = enumeration("fulfillment", FulfillmentType::class)
    val customerType = enumeration("customerType", CustomerType::class).nullable()
    val customerSessionId = uuid("customerSessionId").nullable()
    val customerBusinessType = enumeration("customerBusinessType", BusinessType::class).nullable()
    val customerCompanyName = varchar("customerCompanyName", 50).nullable()
    val customerCompanyTaxId = varchar("customerCompanyTaxId", 50).nullable()
    val customerCustomerTaxId = varchar("customerCustomerTaxId", 50).nullable()
    val customerEmail = varchar("customerEmail", 50).nullable()
    val customerNameFirstname = varchar("customerNameFirstname", 50).nullable()
    val customerNameLastname = varchar("customerNameLastname", 50).nullable()
    val shippingAddressCountry = varchar("shippingAddressCountry", 50).nullable()
    val shippingAddressCity = varchar("shippingAddressCity", 50).nullable()
    val shippingAddressZipCode = varchar("shippingAddressZipCode", 50).nullable()
    val shippingAddressStreet = varchar("shippingAddressStreet", 50).nullable()
    val shippingAddressHouseNumber = varchar("shippingAddressHouseNumber", 50).nullable()
    val billingAddressCountry = varchar("billingAddressCountry", 50).nullable()
    val billingAddressCity = varchar("billingAddressCity", 50).nullable()
    val billingAddressZipCode = varchar("billingAddressZipCode", 50).nullable()
    val billingAddressStreet = varchar("billingAddressStreet", 50).nullable()
    val billingAddressHouseNumber = varchar("billingAddressHouseNumber", 50).nullable()
    val grandTotal = money("grandTotal")
    val netTotal = money("netTotal")
    val shippingCostTotal = money("shippingCostTotal")
    val paymentProcessAmountPaid = money("paymentProcessAmountPayed")
    val paymentProcessAmountToPay = money("paymentProcessAmountToPay")
    val paymentProcessAmountToReturn = money("paymentProcessAmountToReturn")
    val paymentProcessStatus = enumeration("paymentProcessStatus", PaymentProcessStatus::class)
    val paymentProcessExternalPaymentRef = uuid("paymentProcessExternalPaymentRef").nullable()
    val orderId = uuid("orderId").nullable()
}

interface VatAmountTable {
    val parentId: Column<UUID>
    val sign: Column<Int>
    val rate: Column<Double>
    val amount: CompositeColumn<MonetaryAmount>
}

object BasketVatAmountTable : VatAmountTable, Table("BasketVatAmountTable") {
    override val parentId = uuid("parentId").index()
    override val sign = integer("sign")
    override val primaryKey = PrimaryKey(parentId, sign)
    override val rate = double("rate")
    override val amount = money("amount")
}

object BasketItemItemCostVatTable : VatAmountTable, Table("BasketItemItemCostVatTable") {
    override val parentId = uuid("parentId").index()
    override val sign = integer("sign")
    override val primaryKey = PrimaryKey(parentId, sign)
    override val rate = double("rate")
    override val amount = money("amount")
}

object BasketItemTotalCostVatTable : VatAmountTable, Table("BasketItemTotalCostVatTable") {
    override val parentId = uuid("parentId").index()
    override val sign = integer("sign")
    override val primaryKey = PrimaryKey(parentId, sign)
    override val rate = double("rate")
    override val amount = money("amount")
}


object BasketItemTable : Table() {
    val basketId = uuid("basketId").index()
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id, name = "PK_BasketItem")
    val productId = uuid("productId")
    val productName = varchar("productName", 50)
    val productVatSign = integer("productVatSign")
    val productVatRate = double("productVatRate")
    val productUpdatedAt = datetime("productUpdatedAt")
    val priceIdOutletId = varchar("priceIdOutletId", 50)
    val priceIdProductId = uuid("priceIdProductId")
    val priceGrossAmount = money("priceGrossAmount")
    val priceUpdatedAt = datetime("priceUpdatedAt")
    val shippingCost = money("shippingCost")
    val itemCostGrossAmount = money("itemCostGrossAmount")
    val itemCostNetAmount = money("itemCostNetAmount")
    val calculationShippingCost = money("calculationShippingCost")
    val totalCostGrossAmount = money("totalCostGrossAmount")
    val totalCostNetAmount = money("totalCostNetAmount")
}

object PaymentTable : Table() {
    val basketId = uuid("basketId").index()
    val id = uuid("id").uniqueIndex().autoGenerate()
    override val primaryKey = PrimaryKey(id, name = "PK_PaymentTable")
    val method = enumeration("method", PaymentMethod::class)
    val amountSelected = money("amountSelected")
    val amountUsed = money("amountUsed")
    val amountOverpaid = money("amountOverpaid")
    val status = enumeration("status", PaymentStatus::class)
}