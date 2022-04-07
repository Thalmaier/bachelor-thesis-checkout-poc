package core.domain.payment.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.validation.model.ValidationResult
import javax.money.MonetaryAmount

interface PaymentProcess {

    fun addPayment(basketTotal: MonetaryAmount, payment: Payment, basketDataRepository: BasketDataRepository): PaymentProcessAggregate
    fun calculate(basketTotal: MonetaryAmount, basketDataRepository: BasketDataRepository)
    fun calculate(basketTotal: MonetaryAmount, basketData: BasketData)
    fun initialize(externalPaymentId: ExternalPaymentRef): PaymentProcessAggregate
    fun execute(): PaymentProcessAggregate
    fun cancelPayment(id: PaymentId, basketTotal: MonetaryAmount, basketDataRepository: BasketDataRepository): PaymentProcessAggregate
    fun reset(basketDataRepository: BasketDataRepository): PaymentProcessAggregate
    fun validate(): ValidationResult
    fun getAmountToPay(): MonetaryAmount
    fun getPayments(): Set<Payment>

    fun getExternalPaymentRef(): ExternalPaymentRef?

    @JsonIgnore
    fun isInitialized(): Boolean

    @JsonIgnore fun getBasketId(): BasketId
    fun getStatus(): PaymentProcessStatus
    fun getAmountPaid(): MonetaryAmount
    fun getAmountToReturn(): MonetaryAmount
}
