package secondary.repository.common.relational

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import config.RelationalDatabaseConfig
import javax.sql.DataSource

class DefaultPostgresClient(
    private val config: RelationalDatabaseConfig,
    private val dataSource: HikariDataSource = HikariDataSource(dataSourceConfig(config)),
) : DataSource by dataSource, PostgresClient {

    companion object {
        private fun dataSourceConfig(config: RelationalDatabaseConfig): HikariConfig {
            return HikariConfig().apply {
                jdbcUrl = config.connectionUrl
                driverClassName = config.driverClassName
                username = config.username
                password = config.password
                maximumPoolSize = 20
                minimumIdle = 10
            }
        }
    }

    override fun close() {
        dataSource.close()
    }

}
