package secondary.repository.common.document

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.litote.kmongo.util.KMongoConfiguration

/**
 * A [MongoRepository] for abstract classes.
 * Annotations could be used too, but the property to distinguish the concrete class would be passed to clients as well,
 * we dont want that.
 */
abstract class AbstractMongoRepository<T>(abstract: Class<T>, concrete: Class<out T>) : MongoRepository<T> {

    init {
        KMongoConfiguration.registerBsonModule(
            SimpleModule().addDeserializer(abstract, abstractToConcreteDeserializer(concrete))
        )
    }

    private fun <T> abstractToConcreteDeserializer(clazz: Class<out T>): JsonDeserializer<T> {
        return object : JsonDeserializer<T>() {
            override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): T {
                return p?.let { parser ->
                    parser.codec.treeToValue(parser.codec.readTree(parser), clazz)
                } ?: throw RuntimeException("json parser is empty. Could not deserialize class $clazz")
            }
        }
    }

}
