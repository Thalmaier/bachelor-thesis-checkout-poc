package core.domain.validation.model

import com.sksamuel.hoplite.fp.*

/**
 * Used to validate domain objects containing a list of [Validated]
 */
class ValidationResult(private val errors: MutableList<Validated> = mutableListOf()) {

    /**
     * Adds another [Validated] to the list
     */
    fun addResults(vararg items: Validated): ValidationResult = this.apply { errors.addAll(items) }

    /**
     * Throws the passed [Throwable] if at least one result was invalid
     */
    fun throwIfInvalid(lazyException: (List<Invalid>) -> Throwable) {
        this.combine().onInvalid { invalids -> throw lazyException(invalids.list) }
    }

    fun onInvalid(function: (NonEmptyList<Invalid>) -> Unit) {
        this.combine().onInvalid { function(it) }
    }

    /**
     * Combines all invalid results into one or if no result was invalid sets it to [Valid]
     */
    private fun combine(): Validated {
        val errors = errors.mapNotNull { result -> result.fold({ it.list }, { null }) }.flatten()
        return when {
            errors.isNotEmpty() -> errors.nel().invalid()
            else -> Valid.valid()
        }
    }

}
