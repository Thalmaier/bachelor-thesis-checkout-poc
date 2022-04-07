package core.domain.payment.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.common.Entity
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.shipping.service.ShippingCostService
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

/**
 * Entity for containing all information of a payment
 */
class Payment(
    val id: PaymentId = PaymentId(),
    val method: PaymentMethod,
    val amountSelected: MonetaryAmount,
    _amountUsed: MonetaryAmount = Money.zero(amountSelected.currency),
    _amountOverpaid: MonetaryAmount = Money.zero(amountSelected.currency),
    _status: PaymentStatus = PaymentStatus.SELECTED,
) : Entity(id), Comparable<Payment> {

    var amountUsed: MonetaryAmount = _amountUsed
        private set
    var amountOverpaid: MonetaryAmount = _amountOverpaid
        private set
        get() = calculateAmountOverpaid()
    var status: PaymentStatus = _status
        private set

    /**
     * Calculates how much [MonetaryAmount] this [Payment] actually covers
     */
    fun calculateUsage(amountToPay: MonetaryAmount): Payment = this.apply {
        val maxAmountUsageLimit = getMaximumChargeableAmount(amountToPay)
        this.amountUsed = when {
            amountToPay.isGreaterThanOrEqualTo(amountSelected) -> maxAmountUsageLimit
            else -> amountToPay
        }
    }

    /**
     * Gets the maximum amount the payment can cover.
     * Either the [amountSelected] or if it is empty then the [amountToPay]
     */
    private fun getMaximumChargeableAmount(amountToPay: MonetaryAmount): MonetaryAmount {
        return when (amountSelected.isPositive) {
            true -> amountSelected
            false -> amountToPay
        }
    }

    /**
     * Calculates [amountOverpaid] based on [amountSelected] and [amountUsed]
     */
    private fun calculateAmountOverpaid(): MonetaryAmount {
        return if (isCanceled()) {
            return Money.zero(amountSelected.currency)
        } else {
            when (amountUsed.isLessThan(amountSelected)) {
                true -> amountSelected.subtract(amountUsed)
                false -> Money.zero(amountUsed.currency)
            }
        }
    }

    fun select(): Payment = this.apply {
        if (isStatusChangeAllowed()) {
            this.status = PaymentStatus.SELECTED
        }
    }

    fun initialize(): Payment = this.apply {
        if (isStatusChangeAllowed() && status == PaymentStatus.SELECTED) {
            this.status = PaymentStatus.INITIALIZED
        }
    }

    fun execute(): Payment = this.apply {
        if (isStatusChangeAllowed() && status == PaymentStatus.INITIALIZED) {
            this.status = PaymentStatus.EXECUTED
        }
    }

    fun cancel(): Payment = this.apply {
        throwIf(status == PaymentStatus.EXECUTED) { IllegalModificationError("Payment already processed") }
        this.status = PaymentStatus.CANCELED
        this.amountUsed = ShippingCostService.ZERO_MONEY
        this.amountOverpaid = ShippingCostService.ZERO_MONEY
    }

    private fun isStatusChangeAllowed(): Boolean = this.status != PaymentStatus.CANCELED

    @JsonIgnore
    fun isGiftCard(): Boolean = this.method == PaymentMethod.GIFT_CARD

    @JsonIgnore
    fun isCanceled(): Boolean = this.status == PaymentStatus.CANCELED

    override fun compareTo(other: Payment): Int {
        return when (isGiftCard()) {
            true -> -1
            false -> 0
        }
    }

}
