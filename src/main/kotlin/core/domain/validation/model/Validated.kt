package core.domain.validation.model

import com.sksamuel.hoplite.fp.ValidatedNel

/**
 * Result of one single validation result
 */
typealias Validated = ValidatedNel<Invalid, Valid>