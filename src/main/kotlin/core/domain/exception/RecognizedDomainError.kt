package core.domain.exception


/**
 * Represents [RuntimeException] that are known to the domain
 */
open class RecognizedDomainError(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
