package core.domain.validation

import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import core.domain.validation.model.Invalid
import core.domain.validation.model.Valid
import core.domain.validation.model.Validated
import javax.money.MonetaryAmount

/**
 * Returns [Invalid.Null] if null else [Valid]
 */
fun Any?.invalidIfNull(node: String, field: String): Validated {
    return when (this == null) {
        true -> Invalid.Null(node, field).nel().invalid()
        false -> Valid.valid()
    }
}

/**
 * Returns [Invalid.BlankOrNull] if null else [Valid]
 */
fun String?.invalidIfBlank(node: String, field: String): Validated {
    return when (this.isNullOrBlank()) {
        true -> Invalid.BlankOrNull(node, field).nel().invalid()
        false -> Valid.valid()
    }
}

/**
 * Returns [Invalid.Generic] if the regex does not match else [Valid]
 */
fun validIfMatch(
    node: String, field: String, value: String,
    regex: Regex, message: String = "does not match regex",
): Validated {
    return when (value.matches(regex)) {
        true -> Valid.valid()
        false -> Invalid.Generic(node, field, message).nel().invalid()
    }
}

/**
 * Returns [Invalid.Generic] if the boolean is true else [Valid]
 */
fun invalidIf(node: String, field: String, invalid: Boolean, message: String = "invalid"): Validated {
    return when (invalid) {
        true -> Invalid.Generic(node, field, message).nel().invalid()
        false -> Valid.valid()
    }
}

/**
 * Returns [Invalid.RefreshRequired] if the boolean is true else [Valid]
 */
fun requiresRefreshIf(node: String, field: String, invalid: Boolean): Validated {
    return when (invalid) {
        true -> Invalid.RefreshRequired(node, field).nel().invalid()
        false -> Valid.valid()
    }
}

/**
 * Returns [Invalid.Generic] if the two [MonetaryAmount]s are equal else [Valid]
 */
fun validIfEqual(amount1: MonetaryAmount, amount2: MonetaryAmount, message: String): Validated {
    return invalidIf("basket", "calculationResult", !amount1.isEqualTo(amount2), message)
}
