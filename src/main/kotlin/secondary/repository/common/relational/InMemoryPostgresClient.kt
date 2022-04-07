package secondary.repository.common.relational

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import config.RelationalDatabaseConfig
import javax.sql.DataSource

class InMemoryPostgresClient(
    private val config: RelationalDatabaseConfig,
    private val dataSource: HikariDataSource = HikariDataSource(dataSourceConfig(config)),
) : DataSource by dataSource, PostgresClient {

    companion object {
        private fun dataSourceConfig(config: RelationalDatabaseConfig): HikariConfig {
            return HikariConfig().apply {
                username = "postgres"
                jdbcUrl = "jdbc:postgresql://localhost:${config.inMemoryPostgresPort}/postgres"
            }
        }
    }

    override fun close() {
        dataSource.close()
    }
}
