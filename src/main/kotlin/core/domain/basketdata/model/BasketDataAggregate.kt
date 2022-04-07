package core.domain.basketdata.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore
import config.Config
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.*
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.order.model.Order
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.shipping.model.ProductsShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.ValidationResult
import org.bson.codecs.pojo.annotations.BsonId

/**
 * Implementation of a [BasketData]
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
class BasketDataAggregate(
    @BsonId val id: BasketId,
    private val outletId: OutletId,
    private var status: BasketStatus = BasketStatus.OPEN,
    private val items: MutableList<BasketItem> = mutableListOf(),
    private var order: Order? = null,
) : BasketData, Entity(id) {

    override fun addBasketItem(product: Product, price: Price) = this.apply {
        validateIfModificationIsAllowed() and validateMaxItemAmountForNewItems(product.id)
        val basketItem = BasketItem(id = BasketItemId(), product = product, price = price)
        items.add(basketItem)
    }

    override fun addBasketItemAndRecalculate(
        product: Product, price: Price, checkoutData: CheckoutData,
        shippingCostService: ShippingCostService,
        basketCalculationService: BasketCalculationService,
    ) = this.apply {
        addBasketItem(product, price)
        val shippingCosts = shippingCostService.calculateShippingCost(this, checkoutData)
        updateShippingCostAndRecalculateBasket(shippingCosts, basketCalculationService)
    }

    /**
     * Validates if the maximal amount of basket items or the maximum amount of the same product
     * within a [BasketData] is surpassed.
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

    override fun removeBasketItem(basketItemId: BasketItemId) = this.apply {
        validateIfModificationIsAllowed()
        items.find { it.id == basketItemId }?.let { basketItem ->
            items.remove(basketItem)
        } ?: throw ResourceNotFoundError("item", basketItemId)
    }

    override fun removeBasketItemAndRecalculate(
        basketItemId: BasketItemId,
        basketCalculationService: BasketCalculationService,
    ) = this.apply {
        validateIfModificationIsAllowed()
        val itemRemoved = items.find { it.id == basketItemId }?.let { basketItem ->
            items.remove(basketItem)
        } ?: throw ResourceNotFoundError("item", basketItemId)

        if (itemRemoved) {
            basketCalculationService.recalculateIfNecessaryAndSave(id, this)
        }
    }

    override fun finalize(): BasketData = this.apply {
        throwIf(!this.isFrozen()) { IllegalModificationError("Cannot finalize basket if it is not frozen") }
        this.status = BasketStatus.FINALIZED
    }

    override fun unfreeze(): BasketData = this.apply {
        throwIf(!isFrozen()) { IllegalModificationError("Basket should be frozen") }
        this.status = BasketStatus.OPEN
    }

    override fun requiresPriceRefresh(): Boolean {
        return items.any { item -> item.requiresPriceRefresh() }
    }

    override fun refreshPrices(pricePort: PricePort): ModifiedResult<BasketData> {
        validateIfModificationIsAllowed()
        return updateBasketItems { item -> item.refreshPrice(pricePort) }.toRefreshResult(this)
    }

    override fun requiresProductRefresh(): Boolean {
        return items.any { item -> item.requiresProductRefresh() }
    }

    override fun refreshProducts(productPort: ProductPort): ModifiedResult<BasketData> {
        validateIfModificationIsAllowed()
        return updateBasketItems { item -> item.refreshProduct(productPort) }.toRefreshResult(this)
    }

    /**
     * Recalculate all [BasketItem]s and update their shipping costs
     */
    override fun updateShippingCostAndRecalculateBasket(
        shippingCosts: ProductsShippingCost,
        basketCalculationService: BasketCalculationService,
    ): ModifiedResult<Unit> {
        return updateBasketItems { item ->
            val shippingCost = shippingCosts.getOrDefault(item.getProductId(), ShippingCostService.ZERO_MONEY)
            item.calculate(shippingCost)
        }.also { result ->
            if (result.modified) {
                basketCalculationService.recalculateIfNecessaryAndSave(id, this)
            }
        }
    }

    override fun validate(): ValidationResult {
        val node = "basket"
        val result = ValidationResult()
        result.addResults(
            invalidIf(node, "status", this.status != BasketStatus.OPEN, "status is not open"),
            invalidIf(node, "items", this.items.isEmpty(), "basket has no items")
        )
        this.items.forEach { item -> item.validate(node, result) }
        return result
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

    override fun validateIfModificationIsAllowed() {
        throwIf(!canBeModified()) {
            IllegalModificationError("basket cannot be modified while in state $status")
        }
    }

    override fun cancel(): BasketData = this.also {
        throwIf(isFrozen()) { IllegalModificationError("cannot cancel a basket that is frozen") }
        this.status = BasketStatus.CANCELED
    }

    override fun freeze(): BasketData = this.apply {
        validateIfModificationIsAllowed()
        this.status = BasketStatus.FROZEN
    }

    override fun setOrder(order: Order): BasketData = this.apply {
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
    override fun getOrder(): Order? = this.order
    override fun getItems(): List<BasketItem> = this.items

    @JsonIgnore
    override fun isFrozen(): Boolean = this.status == BasketStatus.FROZEN

    @JsonIgnore
    override fun canBeModified(): Boolean = isOpen()

    @JsonIgnore
    override fun isOpen(): Boolean = this.status == BasketStatus.OPEN

    @JsonIgnore
    override fun getProductIdList(): List<ProductId> = this.items.map(BasketItem::getProductId)

}
