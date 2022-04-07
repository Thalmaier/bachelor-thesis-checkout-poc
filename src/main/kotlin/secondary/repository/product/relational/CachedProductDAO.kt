package secondary.repository.product.relational

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object CachedProductTable : Table() {
    val productId = uuid("productId").uniqueIndex()
    override val primaryKey = PrimaryKey(productId)
    val productName = varchar("productName", 50)
    val productVatSign = integer("productVatSign")
    val productVatRate = double("productVatRate")
    val productUpdatedAt = datetime("productUpdatedAt")
    val insertedAt = datetime("insertedAt")
}