package secondary.adapter.exception

/**
 * Thrown if the call to an external resource failed
 */
class FetchingExternalResourceError(message: String?) : RuntimeException(message)
