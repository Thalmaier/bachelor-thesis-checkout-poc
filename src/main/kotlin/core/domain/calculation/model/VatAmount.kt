package core.domain.calculation.model

import java.math.BigDecimal
import javax.money.MonetaryAmount

/**
 * Represents a vat object with [rate], [amount] and [sign].
 * The [sign] identifies the type of the vat.
 */
data class VatAmount(
    val sign: Int,
    val rate: BigDecimal,
    val amount: MonetaryAmount,
) {

    fun add(amount: MonetaryAmount): VatAmount {
        return VatAmount(
            sign = this.sign,
            rate = this.rate,
            amount = this.amount.add(amount)
        )
    }

}
