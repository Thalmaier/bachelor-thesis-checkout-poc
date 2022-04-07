package core.domain.basket.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basket.FulfillmentPort
import core.domain.basket.model.customer.Customer
import core.domain.calculation.model.BasketCalculationResult
import core.domain.calculation.model.BasketItemCalculationResult
import core.domain.common.ModifiedResult
import core.domain.exception.IllegalModificationError
import core.domain.order.model.Order
import core.domain.payment.model.*
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.shipping.model.ProductsShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.model.ValidationResult
import core.domain.validation.service.ValidationService

/**
 * Represents the aggregate root of the whole data model
 */
interface Basket {

    /**
     * Add a new [BasketItem] to the [Basket]. Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun addBasketItem(product: Product, price: Price): Basket

    /**
     * Add a new [BasketItem] to the [Basket] by calling [addBasketItemAndRecalculate] and recalculates the [Basket]
     * @param shippingCostService used to determine the shipping cost of the new basket items
     */
    fun addBasketItemAndRecalculate(
        product: Product, price: Price,
        shippingCostService: ShippingCostService,
    ): Basket

    /**
     * Removes a [BasketItem] from the [Basket]. Triggers [calculateAndUpdate] if an item was removed.
     * Calls [validateIfModificationIsAllowed] beforehand.
     * @param recalculate if true recalculates the basket after removing the [BasketItem]
     */
    fun removeBasketItem(basketItemId: BasketItemId, recalculate: Boolean = true): Basket

    /**
     * Cancels the [Basket] hindering all future changes to it. Only applicable if the [Basket] is not [BasketStatus.FROZEN]
     */
    fun cancel(): Basket

    /**
     * Puts the [Basket] in [BasketStatus.FINALIZED]. Only applicable if the [Basket] is in [BasketStatus.FROZEN]
     */
    fun finalize(): Basket

    /**
     * Returns true if the [Price] is older than a set threshold
     */
    fun requiresPriceRefresh(): Boolean

    /**
     * Refreshes all [Price]s of the [BasketItem]s
     * @return [ModifiedResult.Refreshed] if a [Price] was refreshed, else [ModifiedResult.Unchanged]
     */
    fun refreshPrices(pricePort: PricePort): ModifiedResult<Basket>

    /**
     * Returns true if the [Product] is older than a set threshold
     */
    fun requiresProductRefresh(): Boolean

    /**
     * Refreshes all [Product]s of the [BasketItem]s
     * @return [ModifiedResult.Refreshed] if a [Product] was refreshed, else [ModifiedResult.Unchanged]
     */
    fun refreshProducts(productPort: ProductPort): ModifiedResult<Basket>

    /**
     * Recalculates the [BasketCalculationResult] and all [BasketItemCalculationResult]s of the [BasketItem]s
     * @return [ModifiedResult.Updated] if at least one [BasketItem] was updated, else [ModifiedResult.Unchanged]
     */
    fun calculateAndUpdate(productsShippingCost: ProductsShippingCost): ModifiedResult<Basket>

    /**
     * Recalculates the [BasketCalculationResult] and all [BasketItemCalculationResult]s of the [BasketItem]s
     */
    fun calculateAndUpdate(shippingCostService: ShippingCostService): ModifiedResult<Basket>

    /**
     * Refresh [BasketCalculationResult] and [PaymentProcess] based on the [BasketItem]s
     */
    fun updateBasketCalculationResultAndPaymentProcess()

    /**
     * Adds a new [Payment] to the [Basket] and recalculates the [PaymentProcess]
     * Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun addPaymentAndRecalculatePaymentProcess(payment: Payment): Basket

    /**
     * Cancels a [Payment]. A [Payment] is never deleted, just set to [PaymentStatus.CANCELED]
     * Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun cancelPaymentById(paymentId: PaymentId): Basket

    /**
     * Initializes the [PaymentProcess] by setting the [ExternalPaymentRef]. This sets the [Basket] to [BasketStatus.FROZEN].
     * Calls [validateIfModificationIsAllowed] and [validateBasketState] beforehand.
     */
    fun initializePaymentProcessAndFreezeBasket(externalPaymentId: ExternalPaymentRef, validationService: ValidationService): Basket

    /**
     * Executes the [PaymentProcess]. The basket has to be [BasketStatus.FROZEN]
     */
    fun executePayments(): Basket

    /**
     * Resets the [Basket] and [PaymentProcess]. Sets the basket to [BasketStatus.OPEN].
     * The basket has to be [BasketStatus.FROZEN]
     */
    fun unfreezeAndResetBasket(): Basket

    /**
     * Validates the basket and all its content.
     * @return [ValidationResult]
     */
    fun validateBasketState(): ValidationResult

    /**
     * Validates if any changes are allowed to be made to the basket depending on the [BasketStatus]
     * @throws [IllegalModificationError] if changes to the basket data are currently not allowed
     */
    fun validateIfModificationIsAllowed()

    /**
     * Sets the [FulfillmentType]. Validates if the passed [FulfillmentType] is within the available [FulfillmentType]s according to
     * [FulfillmentPort.getPossibleFulfillment]. Also recalculates the basket if the new
     * [FulfillmentType] is different to the old one.
     * @throws IllegalModificationError if [validateIfModificationIsAllowed] fails or the [FulfillmentType] is invalid
     */
    fun setFulfillment(fulfillment: FulfillmentType, fulfillmentPort: FulfillmentPort, shippingCostService: ShippingCostService): Basket

    /**
     * Sets the [shipping address][Address]. Also recalculates the basket if the new
     * [shipping address][Address] is different to the old one. Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun setShippingAddress(shippingAddress: Address, shippingCostService: ShippingCostService): Basket
    fun setCustomer(customer: Customer)
    fun setBillingAddress(billingAddress: Address): Basket
    fun setOrder(order: Order): Basket

    /**
     * Returns a list of [BasketItem] with the same [ProductId] as the [BasketItem] of the passed [BasketItemId]
     */
    @JsonIgnore
    fun getBasketItemsWithSameProduct(basketItemId: BasketItemId): List<BasketItem>

    @JsonIgnore
    fun getBasketId(): BasketId
    fun getOutletId(): OutletId
    fun getStatus(): BasketStatus
    fun getCustomer(): Customer?
    fun getFulfillment(): FulfillmentType
    fun getShippingAddress(): Address?
    fun getBillingAddress(): Address?
    fun getCalculationResult(): BasketCalculationResult
    fun getItems(): List<BasketItem>
    fun getPaymentProcess(): PaymentProcess
    fun canBeModified(): Boolean
    fun getOrder(): Order?

    @JsonIgnore
    fun isPaymentInitialized(): Boolean

    @JsonIgnore
    fun getProductIdList(): List<ProductId>

    @JsonIgnore
    fun isOpen(): Boolean

    @JsonIgnore
    fun isFrozen(): Boolean
}
