package core.domain.checkoutdata.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.FulfillmentPort
import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.Customer
import core.domain.common.Entity
import core.domain.common.throwIf
import core.domain.exception.IllegalModificationError
import core.domain.validation.invalidIfNull
import core.domain.validation.model.ValidationResult
import org.bson.codecs.pojo.annotations.BsonId

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
class CheckoutDataAggregate(
    @BsonId val id: BasketId,
    private var fulfillmentType: FulfillmentType = FulfillmentType.DELIVERY,
    private var customer: Customer? = null,
    private var billingAddress: Address? = null,
    private var shippingAddress: Address? = null,
    private var outdated: Boolean = true,
) : CheckoutData, Entity(id) {

    override fun validate(): ValidationResult {
        val node = "checkoutData"
        return ValidationResult().apply {
            if (fulfillmentType != FulfillmentType.PICKUP) {
                addResults(
                    customer.invalidIfNull(node, "customer"),
                    shippingAddress.invalidIfNull(node, "shippingAddress"),
                    billingAddress.invalidIfNull(node, "billingAddress")
                )
            }
            customer?.validate(node, this)
            shippingAddress?.validate(node, this)
            billingAddress?.validate(node, this)
        }

    }

    override fun setCustomer(customer: Customer, basketDataRepository: BasketDataRepository) {
        validateIfModificationIsAllowed(basketDataRepository)
        this.customer = customer
    }

    override fun setFulfillment(
        fulfillment: FulfillmentType,
        fulfillmentPort: FulfillmentPort,
        basketDataRepository: BasketDataRepository,
    ) {
        val basketData = validateIfModificationIsAllowed(basketDataRepository)
        if (this.fulfillmentType != fulfillment) {
            val availableFulfillment = fulfillmentPort.getPossibleFulfillment(basketData.getOutletId())

            throwIf(!availableFulfillment.contains(fulfillment)) {
                IllegalModificationError("cannot select $fulfillment for outlet ${basketData.getOutletId()}")
            }

            setOutdated(true)

            this.fulfillmentType = fulfillment
        }
    }

    override fun setShippingAddress(shippingAddress: Address, basketDataRepository: BasketDataRepository) {
        validateIfModificationIsAllowed(basketDataRepository)
        setOutdated(true)
        this.shippingAddress = shippingAddress
    }

    override fun setBillingAddress(billingAddress: Address, basketDataRepository: BasketDataRepository) {
        validateIfModificationIsAllowed(basketDataRepository)
        this.billingAddress = billingAddress
    }


    override fun getBasketId(): BasketId = this.id

    override fun getCustomer(): Customer? = this.customer

    override fun getFulfillment(): FulfillmentType = this.fulfillmentType

    override fun getShippingAddress(): Address? = this.shippingAddress

    override fun getBillingAddress(): Address? = this.billingAddress

    override fun getOutdated(): Boolean = this.outdated
    override fun setOutdated(outdated: Boolean) {
        this.outdated = outdated
    }

    private fun validateIfModificationIsAllowed(basketDataRepository: BasketDataRepository): BasketData {
        return basketDataRepository.findStaleBasketData(id).also { basketData ->
            basketData.validateIfModificationIsAllowed()
        }
    }
}
