package secondary.repository.basketdata.relational

import core.domain.basketdata.model.BasketStatus
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.CompositeColumn
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import secondary.repository.common.relational.money
import java.util.*
import javax.money.MonetaryAmount

object BasketDataTable : Table() {
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id, name = "PK_BasketAggregate")
    val outletId = varchar("outletId", 50)
    val status = enumeration("status", BasketStatus::class)
    val orderId = uuid("orderId").nullable()
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

interface VatAmountTable {
    val parentId: Column<UUID>
    val sign: Column<Int>
    val rate: Column<Double>
    val amount: CompositeColumn<MonetaryAmount>
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