package secondary.repository.common.document

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import config.DocumentOrientedDatabaseConfig
import org.litote.kmongo.KMongo

class DefaultMongoClient(
    private val config: DocumentOrientedDatabaseConfig,
    private val mongoClientSettings: MongoClientSettings = settings(config.connectionUrl),
    private val mongoClient: MongoClient = KMongo.createClient(mongoClientSettings),
) : MongoClient by mongoClient {

    companion object {
        private fun settings(connectionUrl: String): MongoClientSettings {
            return MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(connectionUrl))
                .applyToConnectionPoolSettings { settings -> settings.minSize(10) }
                .build()
        }
    }

}
