package secondary.repository.common.relational

import core.domain.common.Transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import secondary.repository.Repository

@Repository
abstract class PostgresRepository(
    private vararg val tables: Table,
) {

    init {
        Transaction {
            SchemaUtils.create(*tables)
        }
    }

}