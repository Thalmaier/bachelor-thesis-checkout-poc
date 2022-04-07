package secondary.repository.common.document

import com.mongodb.client.MongoClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object MongoDB : KoinComponent {

    val client: MongoClient by inject()

    fun close() {
        client.close()
    }

}
