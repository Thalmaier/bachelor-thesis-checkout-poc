package primary.common

/**
 * Thrown if the request has invalid or missing parameter or request body
 */
class BadRequestError(message: String) : RuntimeException(message)
