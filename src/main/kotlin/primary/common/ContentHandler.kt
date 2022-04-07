package primary.common

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import org.zalando.jackson.datatype.money.MoneyModule
import org.zalando.problem.jackson.ProblemModule
import java.text.DateFormat

/**
 * Mixin class for setting the visibility of all fields of an [Throwable].
 * This disables the deserialization of the [StackTraceElement]s
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
internal abstract class ThrowableAnnotations

/**
 * Configures the de- and serialization of request payloads
 */
val jacksonModules: ObjectMapper.() -> Unit = {
    registerModules(MoneyModule(), ProblemModule().withStackTraces(false), JavaTimeModule())
    dateFormat = DateFormat.getDateTimeInstance()
    setDefaultVisibility(
        JsonAutoDetect.Value.construct(
            PropertyAccessor.FIELD,
            JsonAutoDetect.Visibility.DEFAULT
        )
    )
    addMixIn(Throwable::class.java, ThrowableAnnotations::class.java)
    enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

/**
 * Installs all necessary features for the server application like handling sending and receiving of data
 */
fun Application.contentHandler() {
    install(ContentNegotiation) {
        jackson() {
            jacksonModules()
        }
    }
}
