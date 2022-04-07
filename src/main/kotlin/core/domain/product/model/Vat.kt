package core.domain.product.model

import java.math.BigDecimal


/**
 * Value object for the vat of a corresponding [Product]
 */
data class Vat(
    val sign: Int,
    val rate: BigDecimal,
)
