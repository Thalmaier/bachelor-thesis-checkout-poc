package secondary.repository.common.relational

import core.application.metric.Metric
import core.domain.shipping.service.ShippingCostService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.money.compositeMoney
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import javax.money.MonetaryAmount

fun Table.money(name: String): CompositeColumn<MonetaryAmount> {
    val func: (Table) -> CompositeColumn<MonetaryAmount> = {
        compositeMoney(19, 2, "${name}Amount", "${name}Currency").default(ShippingCostService.ZERO_MONEY)
    }
    return func(this)
}

inline fun Table.metricSelect(crossinline where: SqlExpressionBuilder.() -> Op<Boolean>) = run {
    Metric.read(this.tableName)
    this.select(where)
}

fun <T : Table> T.insertOrUpdate(vararg keys: Column<*>, body: T.(InsertStatement<Number>) -> Unit) =
    InsertOrUpdate<Number>(keys, this).apply {
        body(this)
        execute(TransactionManager.current())
    }

class InsertOrUpdate<Key : Any>(
    private val keys: Array<out Column<*>>,
    table: Table,
    isIgnore: Boolean = false,
) : InsertStatement<Key>(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        Metric.write(table.tableName)
        val updateSetter = super.values.keys.joinToString { "\"${it.name}\" = excluded.\"${it.name}\"" }
        val keyColumns = keys.joinToString(",") { "\"${it.name}\"" }
        val onConflict = "ON CONFLICT ($keyColumns) DO UPDATE SET $updateSetter;"
        return "${super.prepareSQL(transaction)} $onConflict"
    }
}
