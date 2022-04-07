package secondary.repository.common.relational

import java.io.Closeable
import javax.sql.DataSource

interface PostgresClient : Closeable, DataSource
