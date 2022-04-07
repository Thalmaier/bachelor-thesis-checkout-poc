package core.domain.exception


/**
 * Thrown if no modification is currently allowed on the object
 */
class IllegalModificationError(message: String) : RecognizedDomainError(message)
