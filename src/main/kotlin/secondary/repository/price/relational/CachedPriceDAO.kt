package secondary.repository.price

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import secondary.repository.common.relational.money

object CachedPriceTable : Table() {
    val productId = uuid("productId")
    val outletId = varchar("outletId", 50)
    override val primaryKey = PrimaryKey(productId, outletId)
    val grossAmount = money("grossAmount")
    val productUpdatedAt = datetime("productUpdatedAt")
    val insertedAt = datetime("insertedAt")
}