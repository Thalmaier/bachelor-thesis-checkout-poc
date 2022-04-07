package core.domain.payment.service

import core.domain.aggregate.Aggregates
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.service.BasketDataRefreshService
import core.domain.calculation.BasketCalculationRepository
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.common.DomainService
import core.domain.common.Transaction
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.order.service.OrderService
import core.domain.payment.PaymentPort
import core.domain.payment.PaymentProcessRepository
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcess
import core.domain.validation.service.ValidationService
import mu.KotlinLogging

@DomainService
class PaymentProcessDomainService(
    private val basketDataRepository: BasketDataRepository,
    private val paymentPort: PaymentPort,
    private val validationService: ValidationService,
    private val orderService: OrderService,
    private val checkoutDataRepository: CheckoutDataRepository,
    private val basketDataRefreshService: BasketDataRefreshService,
    private val paymentProcessRepository: PaymentProcessRepository,
    private val basketCalculationRepository: BasketCalculationRepository,
) : PaymentProcessApiPort {

    private val logger = KotlinLogging.logger {}

    override fun getPaymentProcess(basketId: BasketId): Aggregates {
        return Aggregates(paymentProcessRepository.findPaymentProcess(basketId))
    }

    override fun getAvailablePaymentMethods(basketId: BasketId): Set<PaymentMethod> {
        return Transaction {
            val basketData = basketDataRefreshService.getRefreshedBasketData(basketId)
            val checkoutData = checkoutDataRepository.findCheckoutData(basketId)
            val basketCalculation = basketCalculationRepository.findBasketCalculation(basketId)
            logger.info { "Determine available payment methods for basket $basketId" }
            paymentPort.determineAvailablePaymentMethods(basketData, checkoutData, basketCalculation)
        }
    }

    override fun cancelPayment(basketId: BasketId, paymentId: PaymentId): Aggregates {
        return Transaction {
            paymentProcessRepository.findPaymentProcess(basketId).let { paymentProcess ->
                val basketCalculation = basketCalculationRepository.findBasketCalculation(basketId)
                paymentProcess.cancelPayment(paymentId, basketCalculation.getGrandTotal(), basketDataRepository)
                paymentProcessRepository.save(paymentProcess)
                logger.info { "Canceled payment $paymentId for basket $basketId" }
                Aggregates(paymentProcess)
            }
        }
    }

    override fun addPayment(basketId: BasketId, payment: Payment): Aggregates {
        return Transaction {
            paymentProcessRepository.findPaymentProcess(basketId).let { paymentProcess ->
                val basketCalculation = basketCalculationRepository.findBasketCalculation(basketId)
                paymentProcess.addPayment(basketCalculation.getGrandTotal(), payment, basketDataRepository)
                paymentProcessRepository.save(paymentProcess)
                logger.info { "Stored basket $basketId with new payment ${payment.id} " }
                Aggregates(paymentProcess)
            }
        }
    }

    override fun initializePaymentProcessAndFreezeBasket(basketId: BasketId): Aggregates {
        return Transaction {
            paymentProcessRepository.findPaymentProcess(basketId).let { paymentProcess ->
                throwIf(paymentProcess.isInitialized()) {
                    IllegalModificationError("payment progress already initialized")
                }
                logger.info { "Initialize payment and freeze basket $basketId" }
                val basketData = basketDataRefreshService.getRefreshedBasketData(basketId)
                validationService.validateAndThrowIfInvalid(basketData, paymentProcess)
                basketData.freeze()
                val checkoutData = checkoutDataRepository.findCheckoutData(basketId)
                val externalPaymentRef = paymentPort.createPaymentProcess(basketData, paymentProcess, checkoutData)
                paymentProcess.initialize(externalPaymentRef)

                initializeExternalPaymentProcess(paymentProcess)
                basketDataRepository.save(basketData)
                paymentProcessRepository.save(paymentProcess)
                logger.info { "Basket was successfully initialized and saved" }
                Aggregates(paymentProcess = paymentProcess, basketData = basketData)
            }
        }
    }

    /**
     * Initializes the payment process in the external system too.
     */
    private fun initializeExternalPaymentProcess(paymentProcess: PaymentProcess) {
        throwIf(!paymentProcess.isInitialized()) { IllegalModificationError("Payment is not initialized") }
        val externalPaymentRef = paymentProcess.getExternalPaymentRef()!!
        paymentPort.initializeAllSubPayments(externalPaymentRef, paymentProcess.getPayments())
    }

    override fun executePaymentProcessAndFinalizeBasket(basketId: BasketId): Aggregates {
        return Transaction {
            paymentProcessRepository.findPaymentProcess(basketId).let { paymentProcess ->
                val basketData = basketDataRepository.findBasketData(basketId)
                throwIf(!basketData.isFrozen() || !paymentProcess.isInitialized()) {
                    IllegalModificationError("cannot execute payment progress if it is not initialized")
                }
                val externalPaymentRef = paymentProcess.getExternalPaymentRef()!!
                paymentPort.executePayment(externalPaymentRef)
                paymentProcess.execute()
                basketData.finalize()
                paymentProcessRepository.save(paymentProcess)
                basketDataRepository.save(basketData)
                logger.info { "Basket payment was executed, saved and finalized" }
                createOrderAfterFinalization(basketData, paymentProcess)
                Aggregates(paymentProcess = paymentProcess, basketData = basketData)
            }
        }
    }

    private fun createOrderAfterFinalization(basketData: BasketData, paymentProcess: PaymentProcess) {
        orderService.createOrder(basketData, paymentProcess)
    }

    override fun cancelPaymentProcessAndResetBasket(basketId: BasketId): Aggregates {
        paymentProcessRepository.findPaymentProcess(basketId).also { paymentProcess ->
            throwIf(!paymentProcess.isInitialized()) {
                IllegalModificationError("cannot cancel payment progress if it is not initialized")
            }
            val basketData = basketDataRepository.findBasketData(basketId)
            basketData.unfreeze()
            basketDataRepository.save(basketData)
            paymentProcess.reset(basketDataRepository)
            paymentProcessRepository.save(paymentProcess)
            logger.info { "Payment process for basket $basketId was canceled" }
            return Aggregates(paymentProcess)
        }
    }

}
