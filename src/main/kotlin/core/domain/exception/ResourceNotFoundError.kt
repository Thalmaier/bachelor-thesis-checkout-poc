package core.domain.exception

/**
 * Thrown if the requested resource was not found
 */
class ResourceNotFoundError(
    name: String?, id: Any?,
) : RecognizedDomainError("${name ?: "Entity"} not found for id ${id ?: "unknown"}")
