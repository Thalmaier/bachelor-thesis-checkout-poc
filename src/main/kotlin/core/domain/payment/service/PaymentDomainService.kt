package core.domain.payment.service

import core.domain.basket.BasketRepository
import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.common.DomainService
import core.domain.common.Transaction
import core.domain.common.and
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.order.service.OrderService
import core.domain.payment.PaymentPort
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentMethod
import core.domain.validation.service.ValidationService
import mu.KotlinLogging

@DomainService
class PaymentDomainService(
    private val basketRepository: BasketRepository,
    private val paymentPort: PaymentPort,
    private val validationService: ValidationService,
    private val orderService: OrderService,
) : PaymentApiPort {

    private val logger = KotlinLogging.logger {}

    override fun getAvailablePaymentMethods(basketId: BasketId): Set<PaymentMethod> {
        return Transaction {
            val basket = basketRepository.getRefreshedBasket(basketId)
            logger.info { "Determine available payment methods for basket $basketId" }
            paymentPort.determineAvailablePaymentMethods(basket)
        }
    }

    override fun cancelPayment(basketId: BasketId, paymentId: PaymentId): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                basket.cancelPaymentById(paymentId)
                basketRepository.save(basket)
                logger.info { "Canceled payment $paymentId for basket $basketId" }
            }
        }
    }

    override fun addPaymentToBasket(basketId: BasketId, payment: Payment): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                basket.addPaymentAndRecalculatePaymentProcess(payment)
                basketRepository.save(basket)
                logger.info { "Stored basket $basketId with new payment ${payment.id} " }
            }
        }
    }

    override fun initializePaymentProcessAndFreezeBasket(basketId: BasketId): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                throwIf(!basket.isOpen() || basket.isPaymentInitialized()) {
                    IllegalModificationError("payment progress already initialized or the basket is not marked as open")
                }
                logger.info { "Initialize payment and freeze basket $basketId" }
                val externalPaymentRef = paymentPort.createPaymentProcess(basket)
                basket.initializePaymentProcessAndFreezeBasket(externalPaymentRef, validationService)
                initializeExternalPaymentProcess(basket)
                basketRepository.save(basket)
                logger.info { "Basket was successfully initialized and saved" }
            }
        }
    }

    /**
     * Initializes the payment process in the external system too.
     * Call only after calling [Basket.initializePaymentProcessAndFreezeBasket] on the [Basket]
     */
    private fun initializeExternalPaymentProcess(basket: Basket) {
        throwIf(!basket.isPaymentInitialized()) { IllegalModificationError("Payment is not initialized") }
        val externalPaymentRef = basket.getPaymentProcess().externalPaymentRef
            ?: throw IllegalStateException("Payment is not initialized even though basket returns true for it")
        paymentPort.initializeAllSubPayments(externalPaymentRef, basket.getPaymentProcess().payments)
    }

    override fun executePaymentProcessAndFinalizeBasket(basketId: BasketId): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                throwIf(!basket.isFrozen() || !basket.isPaymentInitialized()) {
                    IllegalModificationError("cannot execute payment progress if it is not initialized")
                }
                val externalPaymentRef = basket.getPaymentProcess().externalPaymentRef
                    ?: throw IllegalStateException("payment is not initialized even though basket returns true for it")
                paymentPort.executePayment(externalPaymentRef)
                basket.executePayments() and basket.finalize()
                basketRepository.save(basket)
                logger.info { "Basket payment was executed, saved and finalized" }
                createOrderAfterFinalization(basket)
            }
        }
    }

    private fun createOrderAfterFinalization(basket: Basket) {
        orderService.createOrder(basket)
    }

    override fun cancelPaymentProcessAndResetBasket(basketId: BasketId): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                throwIf(!basket.isPaymentInitialized()) {
                    IllegalModificationError("cannot cancel payment progress if it is not initialized")
                }
                basket.unfreezeAndResetBasket()
                basketRepository.save(basket)
                logger.info { "Payment process for basket $basket was canceled" }
            }
        }
    }

}
