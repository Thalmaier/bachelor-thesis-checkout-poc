package primary.common

import com.fasterxml.jackson.core.JacksonException
import core.domain.exception.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import mu.KLogger
import mu.KotlinLogging
import org.zalando.problem.Problem
import org.zalando.problem.Status
import secondary.adapter.exception.FetchingExternalResourceError

private val fallbackLogger = KotlinLogging.logger {}

/**
 * Installs a feature to handle possible exceptions and answering the request with a corresponding [Problem]
 */
fun Application.apiExceptionHandler(logger: KLogger = fallbackLogger) {
    install(StatusPages) {

        // Secondary Adapter Errors
        exception<FetchingExternalResourceError> {
            logger.info { it.message }
            call.respond(HttpStatusCode.ServiceUnavailable, Problem.valueOf(Status.SERVICE_UNAVAILABLE, it.message))
        }

        // Domain Errors
        exception<BadParameterError> {
            logger.info { it.message }
            badRequest(it.message)
        }
        exception<ValidationError> { validationFailed ->
            logger.info { validationFailed.message }
            problem(
                Problem.builder().withStatus(Status.BAD_REQUEST)
                    .withDetail("validation failed")
                    .with("reason", validationFailed.errors.map { it.message })
                    .build()
            )
        }
        exception<IllegalModificationError> {
            logger.info { it.message }
            badRequest(it.message)
        }
        exception<BadRequestError> {
            logger.info { it.message }
            badRequest(it.message)
        }
        exception<ResourceNotFoundError> {
            logger.info { it.message }
            notFound(it.message)
        }

        // Api Exceptions
        exception<MissingRequestParameterException> {
            logger.info { it.message }
            badRequest(it.message)
        }
        exception<JacksonException> {
            logger.info { "Could not parse request body: ${it.message}" }
            badRequest(it.message)
        }
        exception<BadRequestError> {
            logger.info { it.message }
            badRequest(it.message)
        }

        // Unexpected Exceptions
        exception<RecognizedDomainError> {
            logger.error(it) { it.message }
            internalServerError(it.message)
        }
        exception<Throwable> {
            logger.error(it) { it.message }
            internalServerError(it.message)
        }
    }
}

private suspend fun PipelineContext<*, ApplicationCall>.internalServerError(message: String? = "") {
    call.respond(HttpStatusCode.InternalServerError, Problem.valueOf(Status.INTERNAL_SERVER_ERROR, message))
}

private suspend fun PipelineContext<*, ApplicationCall>.notFound(message: String? = "") {
    call.respond(HttpStatusCode.NotFound, Problem.valueOf(Status.NOT_FOUND, message))
}

private suspend fun PipelineContext<*, ApplicationCall>.badRequest(message: String? = "") {
    call.respond(HttpStatusCode.BadRequest, Problem.valueOf(Status.BAD_REQUEST, message))
}

private suspend fun PipelineContext<*, ApplicationCall>.problem(problem: Problem) {
    call.respond(HttpStatusCode.fromValue(problem.status?.statusCode ?: 500), problem)
}
