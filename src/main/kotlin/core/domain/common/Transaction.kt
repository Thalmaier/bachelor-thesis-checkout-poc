package core.domain.common

import config.Config
import org.jetbrains.exposed.sql.transactions.transaction
import secondary.repository.common.relational.PostgresDB


object Transaction {

    val relational: Boolean by lazy { Config().database.useRelationalDatabase }

    inline operator fun <T> invoke(crossinline block: () -> T): T {
        return if (relational) {
            transaction(PostgresDB.db) {
                block()
            }
        } else {
            block()
        }
    }

}