package secondary.repository.common.document

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import config.DocumentOrientedDatabaseConfig
import org.litote.kmongo.KMongo

class InMemoryMongoClient(
    private val config: DocumentOrientedDatabaseConfig,
    private val mongoClientSettings: MongoClientSettings = settings(config),
    private val client: MongoClient = KMongo.createClient(mongoClientSettings),
) : MongoClient by client {

    companion object {
        private fun settings(config: DocumentOrientedDatabaseConfig): MongoClientSettings {
            return MongoClientSettings.builder()
                .applyConnectionString(
                    ConnectionString(
                        "mongodb://localhost:${config.inMemoryMongodbPort}"
                    )
                )
                .applyToConnectionPoolSettings { settings -> settings.minSize(10) }
                .build()
        }
    }
}
