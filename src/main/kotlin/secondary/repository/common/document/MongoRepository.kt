package secondary.repository.common.document

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import config.Config
import secondary.repository.Repository

/**
 * Repository for a mongodb
 */
@Repository
interface MongoRepository<T> {

    val database: MongoDatabase
        get() = MongoDB.client.getDatabase(Config().database.documentOriented.databaseName)

    val collection: MongoCollection<T>

}
