package secondary.repository.calculation.relational

import org.jetbrains.exposed.sql.Table
import secondary.repository.basketdata.relational.VatAmountTable
import secondary.repository.common.relational.money

object BasketCalculationTable : Table() {
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
    val grandTotal = money("grandTotal")
    val netTotal = money("netTotal")
    val shippingCostTotal = money("shippingCostTotal")
}

object BasketVatAmountTable : VatAmountTable, Table() {
    override val parentId = uuid("parentId").index()
    override val sign = integer("sign")
    override val primaryKey = PrimaryKey(parentId, sign)
    override val rate = double("rate")
    override val amount = money("amount")
}