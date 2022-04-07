package core.domain.validation.model

import com.sksamuel.hoplite.fp.nel

/**
 * Used in [Validated] if the result is invalid for different reasons.
 */
sealed class Invalid(
    val node: String, val field: String,
    private val reason: String, val message: String = "$node.$field: $reason",
) {

    class Null(node: String, field: String) : Invalid(node, field, "null")
    class BlankOrNull(node: String, field: String) : Invalid(node, field, "blank or null")
    class RefreshRequired(node: String, field: String) : Invalid(node, field, "requires refresh")

    /**
     * Used if validation failed for any generic reason not fitting another category
     */
    class Generic(node: String, field: String, reason: String) : Invalid(node, field, reason)

    /**
     * Returns a Non-Empty-List of this object
     */
    fun nel() = listOf(this).nel()

}



