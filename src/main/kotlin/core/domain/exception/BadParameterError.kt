package core.domain.exception

/**
 * Thrown if a passed parameter is invalid in the domain context
 */
class BadParameterError(message: String) : RecognizedDomainError(message) {
    constructor(name: String, value: String) : this("parameter $name has invalid value $value")
}
