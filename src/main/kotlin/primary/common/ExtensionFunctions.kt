package primary.common

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.util.pipeline.*
import java.util.*

/**
 * Creates a [UUID] from a [request parameter][param]
 * @param param name of the parameter in the url
 * @throws [BadRequestError] if the parameter does not exist or is invalid
 */
fun PipelineContext<*, ApplicationCall>.parseUUIDFromParameter(param: String): UUID {
    try {
        return UUID.fromString(fromParameter(param))
    } catch (e: IllegalArgumentException) {
        throw BadRequestError("Parameter does not have a valid UUID format")
    }
}

/**
 * Returns the value of the passed [request parameter][param]
 * @param param name of the parameter in the url
 * @throws [BadRequestError] if the parameter does not exist
 */
fun PipelineContext<*, ApplicationCall>.fromParameter(param: String): String =
    context.parameters[param] ?: throw BadRequestException("Missing parameter $param")
