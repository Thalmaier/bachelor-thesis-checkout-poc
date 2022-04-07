package core.domain.common

import config.Config
import org.jetbrains.exposed.sql.transactions.transaction
import secondary.repository.common.relational.PostgresDB

object Transaction {

    private val relational: Boolean by lazy { Config().database.useRelationalDatabase }

    operator fun <T> invoke(block: () -> T): T {
        return if (relational) {
            transaction(PostgresDB.db) {
                block()
            }
        } else {
            block()
        }
    }

}