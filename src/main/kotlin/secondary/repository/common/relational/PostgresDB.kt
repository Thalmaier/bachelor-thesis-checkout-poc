package secondary.repository.common.relational

import org.jetbrains.exposed.sql.Database
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Closeable

object PostgresDB : KoinComponent, Closeable {

    val db: Database by lazy { Database.connect(dataSource) }
    private val dataSource: PostgresClient by inject()

    override fun close() {
        dataSource.close()
    }
}
