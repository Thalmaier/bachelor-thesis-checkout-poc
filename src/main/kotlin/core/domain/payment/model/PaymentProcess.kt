package core.domain.payment.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basket.model.BasketId
import core.domain.common.Entity
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.ValidationResult
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

/**
 * Entity for handling the payment process
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PaymentProcess(
    val basketId: BasketId,
    val payments: MutableSet<Payment> = hashSetOf(),
    private var amountPaid: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var amountToPay: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var amountToReturn: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var status: PaymentProcessStatus = PaymentProcessStatus.TO_PAY,
    _externalPaymentRef: ExternalPaymentRef? = null,
) : Entity(basketId) {

    var externalPaymentRef: ExternalPaymentRef? = _externalPaymentRef
        set(value) {
            throwIf(value != null && externalPaymentRef != null) {
                IllegalModificationError("Cannot set ref if already ref exists")
            }
            field = value
        }

    /**
     * Add a new payment to the payment process
     */
    fun addPayment(basketTotal: MonetaryAmount, payment: Payment): PaymentProcess = this.also {
        throwIf(basketTotal.isZero) { IllegalModificationError("No payment on empty basket") }
        throwIf(newBasketTotalAlreadyPaid(basketTotal, payment)) {
            IllegalModificationError("basket already fully paid")
        }
        payments.add(payment)
        calculate(basketTotal)
    }

    private fun newBasketTotalAlreadyPaid(basketTotal: MonetaryAmount, payment: Payment): Boolean {
        return basketTotal.isEqualTo(amountPaid) and payments.isNotEmpty() and !payment.isGiftCard()
    }

    /**
     * Calculates the usage of all [Payment]s and updates the [PaymentStatus]
     */
    fun calculate(basketTotal: MonetaryAmount) {
        resetAmounts(basketTotal)
        calculatePayments()
        updateStatus()
    }

    private fun resetAmounts(basketTotal: MonetaryAmount) {
        amountToPay = basketTotal
        amountPaid = Money.zero(amountToPay.currency)
        amountToReturn = Money.zero(amountToPay.currency)
    }

    private fun calculatePayments() {
        payments.sorted().forEach { payment ->
            when {
                isFullyPaid() -> payment.cancel()
                payment.status != PaymentStatus.CANCELED -> {
                    payment.calculateUsage(amountToPay)
                    amountToReturn = payment.amountOverpaid
                    increasePaidAndDecreaseToPay(payment.amountUsed)
                }
            }
        }
    }

    private fun increasePaidAndDecreaseToPay(amount: MonetaryAmount) {
        amountPaid = amountPaid.add(amount)
        amountToPay = amountToPay.subtract(amount)
    }

    private fun updateStatus() {
        status = when {
            amountPaid.isZero -> PaymentProcessStatus.TO_PAY
            amountToPay.isZero -> PaymentProcessStatus.PAID
            else -> PaymentProcessStatus.PARTIALLY_PAID
        }
    }

    /**
     * Initializes the [PaymentProcess] and all contained [Payment]s
     */
    fun initialize(externalPaymentId: ExternalPaymentRef): PaymentProcess = this.apply {
        throwIf(externalPaymentRef != null) { IllegalModificationError("payment process already in progress") }
        this.externalPaymentRef = externalPaymentId
        payments.forEach(Payment::initialize)
    }

    /**
     * Executes the [PaymentProcess] and all contained [Payment]s
     */
    fun execute(): PaymentProcess = this.apply {
        throwIf(externalPaymentRef == null) { IllegalModificationError("payment process not in process") }
        payments.forEach(Payment::execute)
    }

    /**
     * Cancels a certain [Payment] of the payment process
     */
    fun cancelPayment(id: PaymentId, basketTotal: MonetaryAmount): PaymentProcess = this.apply {
        findPayment(id).cancel()
        calculate(basketTotal)
    }

    /**
     * Resets the [PaymentProcess] and all contained [Payment]s
     */
    fun reset(): PaymentProcess = this.apply {
        externalPaymentRef = null
        payments.forEach(Payment::select)
    }

    /**
     * Validates the [PaymentProcess] and all contained [Payment]s
     */
    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.paymentProcess"
        result.addResults(
            invalidIf(node, "payments", getActivePayments().isEmpty(), "should not be empty"),
            invalidIf(node, "fullyPaid", !isFullyPaid(), "should be fully paid")
        )
    }

    private fun findPayment(id: PaymentId): Payment {
        return payments.firstOrNull { it.id == id } ?: throw ResourceNotFoundError("payment", id)
    }

    @JsonIgnore
    private fun isFullyPaid() = amountToPay.isZero

    @JsonIgnore
    private fun getActivePayments() = this.payments.filter { payment -> !payment.isCanceled() }

    fun getStatus() = this.status
    fun getAmountToPay() = this.amountToPay
    fun getAmountToReturn() = this.amountToReturn
    fun getAmountPaid() = this.amountPaid

}
