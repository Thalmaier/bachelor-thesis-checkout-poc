package core.application.basket

import core.application.ApplicationService
import core.domain.basket.BasketFactory
import core.domain.basket.BasketRepository
import core.domain.basket.FulfillmentPort
import core.domain.basket.model.*
import core.domain.basket.model.customer.Customer
import core.domain.common.Transaction
import core.domain.common.anyNotNull
import core.domain.payment.model.Payment
import core.domain.shipping.service.ShippingCostService
import mu.KotlinLogging

/**
 * Implementation of the [BasketApiPort]
 */
@ApplicationService
class BasketApplicationService(
    private val basketRepository: BasketRepository,
    private val basketFactory: BasketFactory,
    private val fulfillmentPort: FulfillmentPort,
    private val shippingCostService: ShippingCostService,
) : BasketApiPort {

    private val logger = KotlinLogging.logger {}

    override fun findBasketById(basketId: BasketId): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId)
        }
    }

    override fun createBasket(outletId: OutletId, customer: Customer?): Basket {
        return basketFactory.createNewBasket(outletId = outletId, customer = customer).also { basket ->
            Transaction {
                basketRepository.save(basket)
                logger.info { "Saved new basket with id ${basket.getBasketId()}" }
            }
        }
    }

    override fun cancelBasket(basketId: BasketId): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                basket.cancel()
                basketRepository.save(basket)
                logger.info { "Canceled basket with id ${basket.getBasketId()}" }
            }
        }
    }

    override fun setCustomer(basketId: BasketId, customer: Customer): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                basket.setCustomer(customer)
                basketRepository.save(basket)
                logger.info { "Saved new customer data to basket ${basket.getBasketId()}" }
            }
        }
    }

    override fun setFulfillment(basketId: BasketId, fulfillmentType: FulfillmentType): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                basket.setFulfillment(fulfillmentType, fulfillmentPort, shippingCostService)
                basketRepository.save(basket)
                logger.info { "Set fulfillment $fulfillmentType to basket ${basket.getBasketId()}" }
            }
        }
    }

    override fun getAvailableFulfillment(basketId: BasketId): List<FulfillmentType> {
        return Transaction {
            val basket = basketRepository.getStaleBasket(basketId)
            fulfillmentPort.getPossibleFulfillment(basket.getOutletId()).also {
                logger.info { "Fetched available fulfillments for basket ${basket.getBasketId()}" }
            }
        }
    }

    override fun setShippingAddress(basketId: BasketId, shippingAddress: Address): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                basket.setShippingAddress(shippingAddress, shippingCostService)
                basketRepository.save(basket)
                logger.info { "Saved new shipping address to basket ${basket.getBasketId()}" }
            }
        }
    }

    override fun setBillingAddress(basketId: BasketId, billingAddress: Address): Basket {
        return Transaction {
            basketRepository.getStaleBasket(basketId).also { basket ->
                basket.setBillingAddress(billingAddress)
                basketRepository.save(basket)
                logger.info { "Saved new billing address to basket ${basket.getBasketId()}" }
            }
        }
    }

    override fun setCheckoutData(
        basketId: BasketId, fulfillment: FulfillmentType?, shippingAddress: Address?,
        billingAddress: Address?, customer: Customer?, payment: Payment?,
    ): Basket {
        return Transaction {
            basketRepository.getRefreshedBasket(basketId).also { basket ->
                fulfillment?.run { basket.setFulfillment(fulfillment, fulfillmentPort, shippingCostService) }
                shippingAddress?.run { basket.setShippingAddress(shippingAddress, shippingCostService) }
                billingAddress?.run { basket.setBillingAddress(billingAddress) }
                customer?.run { basket.setCustomer(customer) }
                payment?.run { basket.addPaymentAndRecalculatePaymentProcess(payment) }

                if (anyNotNull(fulfillment, shippingAddress, billingAddress, customer, payment)) {
                    basketRepository.save(basket)
                    logger.info { "Saved passed checkout data to basket ${basket.getBasketId()}" }
                }
            }
        }
    }
}
