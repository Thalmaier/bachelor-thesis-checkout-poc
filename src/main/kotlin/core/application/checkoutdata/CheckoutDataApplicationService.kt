package core.application.checkoutdata

import core.domain.aggregate.Aggregates
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.Customer
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.Transaction
import core.domain.common.anyNotNull
import core.domain.payment.model.Payment
import core.domain.payment.service.PaymentProcessApiPort
import core.domain.shipping.service.ShippingCostService
import primary.PrimaryAdapter

@PrimaryAdapter
class CheckoutDataApplicationService(
    private val checkoutDataRepository: CheckoutDataRepository,
    private val fulfillmentPort: FulfillmentPort,
    private val paymentApiPort: PaymentProcessApiPort,
    private val basketDataRepository: BasketDataRepository,
    private val shippingCostService: ShippingCostService,
    private val basketCalculationService: BasketCalculationService,
) : CheckoutDataApiPort {

    override fun findCheckoutDataById(basketId: BasketId): Aggregates {
        return Transaction {
            Aggregates(checkoutDataRepository.findCheckoutData(basketId))
        }
    }

    override fun setCustomer(basketId: BasketId, customer: Customer): Aggregates {
        return Transaction {
            checkoutDataRepository.findCheckoutData(basketId).let { checkoutData ->
                checkoutData.setCustomer(customer, basketDataRepository)
                checkoutDataRepository.save(checkoutData)
                Aggregates(checkoutData)
            }
        }
    }

    override fun setFulfillment(basketId: BasketId, fulfillmentType: FulfillmentType): Aggregates {
        return Transaction {
            checkoutDataRepository.findCheckoutData(basketId).let { checkoutData ->
                checkoutData.setFulfillment(fulfillmentType, fulfillmentPort, basketDataRepository,
                    shippingCostService, basketCalculationService)
                checkoutDataRepository.save(checkoutData)
                Aggregates(checkoutData)
            }
        }
    }

    override fun setBillingAddress(basketId: BasketId, billingAddress: Address): Aggregates {
        return Transaction {
            checkoutDataRepository.findCheckoutData(basketId).let { checkoutData ->
                checkoutData.setBillingAddress(billingAddress, basketDataRepository)
                checkoutDataRepository.save(checkoutData)
                Aggregates(checkoutData)
            }
        }
    }

    override fun setShippingAddress(basketId: BasketId, shippingAddress: Address): Aggregates {
        return Transaction {
            checkoutDataRepository.findCheckoutData(basketId).let { checkoutData ->
                checkoutData.setShippingAddress(shippingAddress, basketDataRepository,
                    shippingCostService, basketCalculationService)
                checkoutDataRepository.save(checkoutData)
                Aggregates(checkoutData)
            }
        }
    }


    override fun setCheckoutData(
        basketId: BasketId, fulfillment: FulfillmentType?, shippingAddress: Address?,
        billingAddress: Address?, customer: Customer?, payment: Payment?,
    ): Aggregates {
        return Transaction {
            val paymentBasket = payment?.let { paymentApiPort.addPayment(basketId, it) }
            Aggregates(
                checkoutData = setAllCheckoutData(basketId, fulfillment, shippingAddress, billingAddress, customer),
                paymentProcess = paymentBasket?.paymentProcess
            )
        }
    }

    private fun setAllCheckoutData(
        basketId: BasketId, fulfillment: FulfillmentType?, shippingAddress: Address?,
        billingAddress: Address?, customer: Customer?,
    ): CheckoutData {
        return checkoutDataRepository.findCheckoutData(basketId).also { checkoutData ->
            fulfillment?.run {
                checkoutData.setFulfillment(fulfillment, fulfillmentPort, basketDataRepository,
                    shippingCostService, basketCalculationService)
            }
            shippingAddress?.run {
                checkoutData.setShippingAddress(shippingAddress, basketDataRepository,
                    shippingCostService, basketCalculationService)
            }
            billingAddress?.run { checkoutData.setBillingAddress(billingAddress, basketDataRepository) }
            customer?.run { checkoutData.setCustomer(customer, basketDataRepository) }

            if (anyNotNull(fulfillment, shippingAddress, billingAddress, customer)) {
                checkoutDataRepository.save(checkoutData)
            }
        }
    }

}
