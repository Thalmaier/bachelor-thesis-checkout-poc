package core.domain.payment.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.common.Entity
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.ValidationResult
import org.bson.codecs.pojo.annotations.BsonId
import org.javamoney.moneta.Money
import javax.money.MonetaryAmount

/**
 * Entity for handling the payment process
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PaymentProcessAggregate(
    @BsonId val id: BasketId,
    private val payments: MutableSet<Payment> = mutableSetOf(),
    private var amountPaid: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var amountToPay: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var amountToReturn: MonetaryAmount = ShippingCostService.ZERO_MONEY,
    private var status: PaymentProcessStatus = PaymentProcessStatus.TO_PAY,
    _externalPaymentRef: ExternalPaymentRef? = null,
) : PaymentProcess, Entity(id) {


    private var externalPaymentRef: ExternalPaymentRef? = _externalPaymentRef
        set(value) {
            throwIf(value != null && externalPaymentRef != null) {
                IllegalModificationError("Cannot set ref if already ref exists")
            }
            field = value
        }

    /**
     * Add a new payment to the payment process
     */
    override fun addPayment(
        basketTotal: MonetaryAmount, payment: Payment,
        basketDataRepository: BasketDataRepository,
    ): PaymentProcessAggregate = this.also {
        val basketData = validateIfModificationIsAllowed(basketDataRepository)
        throwIf(basketTotal.isZero) { IllegalModificationError("No payment on empty basket") }
        throwIf(newBasketTotalAlreadyPaid(basketTotal, payment)) {
            IllegalModificationError("basket already fully paid")
        }
        payments.add(payment)
        calculate(basketTotal, basketData)
    }

    private fun newBasketTotalAlreadyPaid(basketTotal: MonetaryAmount, payment: Payment): Boolean {
        return basketTotal.isEqualTo(amountPaid) and payments.isNotEmpty() and !payment.isGiftCard()
    }

    /**
     * Calculates the usage of all [Payment]s and updates the [PaymentStatus]
     */
    override fun calculate(basketTotal: MonetaryAmount, basketDataRepository: BasketDataRepository) {
        calculate(basketTotal, basketDataRepository.findBasketData(id))
    }

    override fun calculate(basketTotal: MonetaryAmount, basketData: BasketData) {
        validateIfModificationIsAllowed(basketData)
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
     * Initializes the [PaymentProcessAggregate] and all contained [Payment]s
     */
    override fun initialize(externalPaymentId: ExternalPaymentRef): PaymentProcessAggregate = this.apply {
        throwIf(externalPaymentRef != null) { IllegalModificationError("payment process already in progress") }
        this.externalPaymentRef = externalPaymentId
        payments.forEach(Payment::initialize)
    }

    /**
     * Executes the [PaymentProcessAggregate] and all contained [Payment]s
     */
    override fun execute(): PaymentProcessAggregate = this.apply {
        throwIf(externalPaymentRef == null) { IllegalModificationError("payment process not in process") }
        payments.forEach(Payment::execute)
    }

    /**
     * Cancels a certain [Payment] of the payment process
     */
    override fun cancelPayment(
        id: PaymentId, basketTotal: MonetaryAmount,
        basketDataRepository: BasketDataRepository,
    ): PaymentProcessAggregate = this.apply {
        val basketData = validateIfModificationIsAllowed(basketDataRepository)
        findPayment(id).cancel()
        calculate(basketTotal, basketData)
    }

    /**
     * Resets the [PaymentProcessAggregate] and all contained [Payment]s
     */
    override fun reset(basketDataRepository: BasketDataRepository): PaymentProcessAggregate = this.apply {
        validateIfModificationIsAllowed(basketDataRepository)
        externalPaymentRef = null
        payments.forEach(Payment::select)
    }

    /**
     * Validates the [PaymentProcessAggregate] and all contained [Payment]s
     */
    override fun validate(): ValidationResult {
        val node = "paymentProcess"
        return ValidationResult().apply {
            addResults(
                invalidIf(node, "payments", getActivePayments().isEmpty(), "should not be empty"),
                invalidIf(node, "fullyPaid", !isFullyPaid(), "should be fully paid")
            )
        }
    }

    private fun validateIfModificationIsAllowed(basketDataRepository: BasketDataRepository): BasketData {
        return basketDataRepository.findBasketData(id).also { basketData ->
            validateIfModificationIsAllowed(basketData)
        }
    }

    private fun validateIfModificationIsAllowed(basketData: BasketData): BasketData {
        return basketData.also { it.validateIfModificationIsAllowed() }
    }

    private fun findPayment(id: PaymentId): Payment {
        return payments.firstOrNull { it.id == id } ?: throw ResourceNotFoundError("payment", id)
    }

    @JsonIgnore
    private fun isFullyPaid() = amountToPay.isZero

    @JsonIgnore
    private fun getActivePayments() = this.payments.filter { payment -> !payment.isCanceled() }

    override fun getBasketId(): BasketId = id
    override fun getStatus() = this.status

    override fun getAmountPaid() = this.amountPaid

    override fun getAmountToReturn(): MonetaryAmount = this.amountToReturn

    override fun isInitialized(): Boolean = externalPaymentRef != null

    override fun getAmountToPay(): MonetaryAmount = this.amountToPay

    override fun getPayments(): Set<Payment> = this.payments.toSet()

    override fun getExternalPaymentRef(): ExternalPaymentRef? = this.externalPaymentRef

}
