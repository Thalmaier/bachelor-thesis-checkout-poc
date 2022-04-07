package core.domain.basket.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import config.Config
import core.domain.basket.FulfillmentPort
import core.domain.basket.model.customer.Customer
import core.domain.calculation.model.BasketCalculationResult
import core.domain.calculation.model.combine
import core.domain.calculation.model.toBasketCalculationResult
import core.domain.common.*
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.order.model.Order
import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentProcess
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.shipping.model.ProductsShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.invalidIfNull
import core.domain.validation.model.ValidationResult
import core.domain.validation.service.ValidationService
import org.bson.codecs.pojo.annotations.BsonId

/**
 * Implementation of a [Basket]
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
class BasketAggregate(
    @BsonId val id: BasketId,
    private val outletId: OutletId,
    private var status: BasketStatus = BasketStatus.OPEN,
    private var customer: Customer? = null,
    private var fulfillment: FulfillmentType = FulfillmentType.DELIVERY,
    private var billingAddress: Address? = null,
    private var shippingAddress: Address? = null,
    private var calculationResult: BasketCalculationResult = BasketCalculationResult(),
    private val items: MutableList<BasketItem> = mutableListOf(),
    private var paymentProcess: PaymentProcess = PaymentProcess(id),
    private var order: Order? = null,
) : Basket, Entity(id) {

    override fun addBasketItem(product: Product, price: Price) = this.apply {
        validateIfModificationIsAllowed() and validateMaxItemAmountForNewItems(product.id)
        val basketItem = BasketItem(id = BasketItemId(), product = product, price = price)
        items.add(basketItem)
    }

    override fun addBasketItemAndRecalculate(product: Product, price: Price, shippingCostService: ShippingCostService) = this.apply {
        addBasketItem(product, price)
        calculateAndUpdate(shippingCostService)
    }

    /**
     * Validates if the maximal amount of basket items or the maximum amount of the same product
     * within a [Basket] is surpassed.
     * @throws IllegalModificationError if the [Product] cannot be added due to a violation.
     */
    private fun validateMaxItemAmountForNewItems(productId: ProductId) {
        val maxItemAmount = Config().businessRules.maxItemAmount
        throwIf(items.size + 1 > maxItemAmount) { IllegalModificationError("Cannot surpass the max item count $maxItemAmount") }

        val maxSameItemCount = Config().businessRules.maxSameItemCount
        val sameProductItems = getBasketItemsWithSameProduct(productId)
        throwIf(sameProductItems.size + 1 > maxSameItemCount) {
            IllegalModificationError("Cannot surpass the max item count $maxSameItemCount for the same product")
        }
    }

    override fun removeBasketItem(basketItemId: BasketItemId, recalculate: Boolean) = this.apply {
        validateIfModificationIsAllowed()
        items.find { it.id == basketItemId }?.let { basketItem ->
            items.remove(basketItem)
            if (recalculate) {
                updateBasketCalculationResultAndPaymentProcess()
            }
        } ?: throw ResourceNotFoundError("item", basketItemId)
    }

    override fun finalize(): Basket = this.apply {
        throwIf(!this.isFrozen()) { IllegalModificationError("Cannot finalize basket if it is not frozen") }
        this.status = BasketStatus.FINALIZED
    }

    override fun unfreezeAndResetBasket(): Basket = this.apply {
        throwIf(!isFrozen()) { IllegalModificationError("Basket should be frozen") }
        this.status = BasketStatus.OPEN
        this.paymentProcess.reset()
    }

    override fun requiresPriceRefresh(): Boolean {
        return items.any { item -> item.requiresPriceRefresh() }
    }

    override fun refreshPrices(pricePort: PricePort): ModifiedResult<Basket> {
        validateIfModificationIsAllowed()
        return updateBasketItems { item -> item.refreshPrice(pricePort) }.toRefreshResult(this)
    }

    override fun requiresProductRefresh(): Boolean {
        return items.any { item -> item.requiresProductRefresh() }
    }

    override fun refreshProducts(productPort: ProductPort): ModifiedResult<Basket> {
        validateIfModificationIsAllowed()
        return updateBasketItems { item -> item.refreshProduct(productPort) }.toRefreshResult(this)
    }

    override fun calculateAndUpdate(shippingCostService: ShippingCostService): ModifiedResult<Basket> {
        return calculateAndUpdate(shippingCostService.calculateShippingCost(this))
    }

    override fun calculateAndUpdate(productsShippingCost: ProductsShippingCost): ModifiedResult<Basket> {
        validateIfModificationIsAllowed()
        val basketItemsUpdateResult = calculateBasketItemsAndUpdateShippingCost(productsShippingCost)
        if (basketItemsUpdateResult.modified) {
            // Recalculate only if at least one basket item was updated
            updateBasketCalculationResultAndPaymentProcess()
        }
        return basketItemsUpdateResult.setPayload(this)
    }


    override fun updateBasketCalculationResultAndPaymentProcess() {
        this.calculationResult = items.map(BasketItem::getCalculationResult).combine().toBasketCalculationResult()
        this.paymentProcess.calculate(calculationResult.grandTotal)
    }

    /**
     * Recalculate all [BasketItem]s and update their shipping costs
     */
    private fun calculateBasketItemsAndUpdateShippingCost(shippingCosts: ProductsShippingCost): ModifiedResult<Unit> {
        return updateBasketItems { item ->
            val shippingCost = shippingCosts.getOrDefault(item.getProductId(), ShippingCostService.ZERO_MONEY)
            item.calculate(shippingCost)
        }
    }

    /**
     * Update all [BasketItem]s with a modification function and return the result.
     * @return [ModifiedResult.Updated] if at least one item was updated, else [ModifiedResult.Unchanged]
     */
    private fun updateBasketItems(update: (BasketItem) -> ModifiedResult<BasketItem>): ModifiedResult<Unit> {
        var updated = false
        items.replaceAll { item ->
            val result = update(item)
            updated = updated || result.modified
            result.payload
        }
        return updated.toUpdateResult(Unit)
    }

    override fun addPaymentAndRecalculatePaymentProcess(payment: Payment): Basket = this.also {
        validateIfModificationIsAllowed()
        this.paymentProcess.addPayment(calculationResult.grandTotal, payment)
    }

    override fun isPaymentInitialized(): Boolean = this.paymentProcess.externalPaymentRef != null

    override fun initializePaymentProcessAndFreezeBasket(
        externalPaymentId: ExternalPaymentRef,
        validationService: ValidationService,
    ): Basket = this.also {
        this.freeze(validationService)
        this.paymentProcess.initialize(externalPaymentId)
    }

    override fun executePayments(): Basket = this.apply {
        throwIf(!isFrozen()) { IllegalModificationError("should be frozen") }
        this.paymentProcess.execute()
    }

    override fun cancelPaymentById(paymentId: PaymentId): Basket = this.apply {
        validateIfModificationIsAllowed()
        this.paymentProcess.cancelPayment(paymentId, calculationResult.grandTotal)
    }

    override fun validateIfModificationIsAllowed() {
        throwIf(!canBeModified()) {
            IllegalModificationError("basket cannot be modified while in state $status")
        }
    }

    override fun validateBasketState(): ValidationResult {
        val node = "basket"
        val result = ValidationResult()
        validateCheckoutData(result, node)
        result.addResults(
            invalidIf(node, "status", this.status != BasketStatus.OPEN, "status is not open"),
            invalidIf(node, "items", this.items.isEmpty(), "basket has no items")
        )
        this.items.forEach { item -> item.validate(node, result) }
        this.paymentProcess.validate(node, result)
        return result
    }

    private fun validateCheckoutData(result: ValidationResult, node: String) {
        if (this.fulfillment != FulfillmentType.PICKUP) {
            result.addResults(
                customer.invalidIfNull(node, "customer"),
                shippingAddress.invalidIfNull(node, "shippingAddress"),
                billingAddress.invalidIfNull(node, "billingAddress")
            )
        }
        this.customer?.validate(node, result)
        this.shippingAddress?.validate(node, result)
        this.billingAddress?.validate(node, result)
    }

    override fun cancel(): Basket = this.also {
        throwIf(isFrozen()) { IllegalModificationError("cannot cancel a basket that is frozen") }
        this.status = BasketStatus.CANCELED
    }

    private fun freeze(validationService: ValidationService): Basket = this.apply {
        validateIfModificationIsAllowed() and validationService.validateAndThrowIfInvalid(this)
        this.status = BasketStatus.FROZEN
    }

    override fun setFulfillment(
        fulfillment: FulfillmentType, fulfillmentPort: FulfillmentPort,
        shippingCostService: ShippingCostService,
    ): Basket = this.apply {
        validateIfModificationIsAllowed()
        if (this.fulfillment != fulfillment) {
            // validate if fulfillment is allowed for this outlet
            val availableFulfillment = fulfillmentPort.getPossibleFulfillment(outletId)
            throwIf(!availableFulfillment.contains(fulfillment)) {
                IllegalModificationError("cannot select $fulfillment for outlet $outletId")
            }

            this.fulfillment = fulfillment

            calculateAndUpdate(shippingCostService)
        }
    }

    override fun setCustomer(customer: Customer) {
        validateIfModificationIsAllowed()
        this.customer = customer
    }

    override fun setShippingAddress(shippingAddress: Address, shippingCostService: ShippingCostService): Basket = this.apply {
        validateIfModificationIsAllowed()
        if (this.shippingAddress != shippingAddress) {
            this.shippingAddress = shippingAddress
            calculateAndUpdate(shippingCostService)
        }
    }

    override fun setBillingAddress(billingAddress: Address): Basket = this.apply {
        validateIfModificationIsAllowed()
        this.billingAddress = billingAddress
    }

    override fun setOrder(order: Order): Basket = this.apply {
        this.order = order
    }

    @JsonIgnore
    override fun getBasketItemsWithSameProduct(basketItemId: BasketItemId): List<BasketItem> {
        return items.firstOrNull { it.id == basketItemId }?.let { basketItem ->
            getBasketItemsWithSameProduct(basketItem.getProductId())
        } ?: emptyList()
    }

    private fun getBasketItemsWithSameProduct(productId: ProductId): List<BasketItem> {
        return this.items.filter { item -> item.getProductId() == productId }
    }

    @JsonIgnore
    override fun getBasketId(): BasketId = this.id
    override fun getOutletId(): OutletId = this.outletId
    override fun getStatus(): BasketStatus = this.status
    override fun getCustomer(): Customer? = this.customer
    override fun getFulfillment(): FulfillmentType = this.fulfillment
    override fun getBillingAddress(): Address? = this.billingAddress
    override fun getCalculationResult(): BasketCalculationResult = this.calculationResult
    override fun getShippingAddress(): Address? = this.shippingAddress
    override fun getItems(): List<BasketItem> = this.items
    override fun getPaymentProcess(): PaymentProcess = this.paymentProcess
    override fun getOrder(): Order? = this.order

    @JsonIgnore
    override fun isFrozen(): Boolean = this.status == BasketStatus.FROZEN

    @JsonIgnore
    override fun canBeModified(): Boolean = isOpen()

    @JsonIgnore
    override fun isOpen(): Boolean = this.status == BasketStatus.OPEN

    @JsonIgnore
    override fun getProductIdList(): List<ProductId> = this.items.map(BasketItem::getProductId)
}
