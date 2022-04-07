package secondary.repository.calculation.relational

import core.domain.basketdata.model.BasketId
import core.domain.calculation.BasketCalculationRepository
import core.domain.calculation.model.BasketCalculation
import core.domain.calculation.model.BasketCalculationAggregate
import org.jetbrains.exposed.sql.ResultRow
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.mapVatAmounts
import secondary.repository.common.relational.metricInsertOrUpdate
import secondary.repository.common.relational.metricSelect

class BasketCalculationPostgreRepository : BasketCalculationRepository, PostgresRepository(
    tables = arrayOf(BasketCalculationTable, BasketVatAmountTable)
) {
    override fun save(basketCalculation: BasketCalculation) {
        BasketCalculationTable.metricInsertOrUpdate(BasketCalculationTable.id) {
            it[id] = basketCalculation.getBasketId().id
            it[grandTotal] = basketCalculation.getGrandTotal()
            it[netTotal] = basketCalculation.getNetTotal()
            it[shippingCostTotal] = basketCalculation.getShippingCostTotal()
        }
        basketCalculation.getVatAmounts().values.forEach { vatAmount ->
            BasketVatAmountTable.metricInsertOrUpdate(BasketVatAmountTable.parentId, BasketVatAmountTable.sign) {
                it[parentId] = basketCalculation.getBasketId().id
                it[sign] = vatAmount.sign
                it[rate] = vatAmount.rate.toDouble()
                it[amount] = vatAmount.amount
            }
        }
    }

    override fun findStaleBasketCalculation(id: BasketId): BasketCalculation {
        val dao = BasketCalculationTable.metricSelect { BasketCalculationTable.id eq id.id }.singleOrNull()
        return dao?.let { mapCalculationResult(it, id) } ?: BasketCalculationAggregate(id)
    }

    private fun mapCalculationResult(dao: ResultRow, id: BasketId): BasketCalculation {
        return BasketCalculationAggregate(
            id = id,
            grandTotal = dao[BasketCalculationTable.grandTotal],
            netTotal = dao[BasketCalculationTable.netTotal],
            shippingCostTotal = dao[BasketCalculationTable.shippingCostTotal],
            vatAmounts = mapVatAmounts(BasketVatAmountTable, id.id),
        )
    }

}