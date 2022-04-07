package core.domain.basketdata.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.calculation.service.BasketCalculationService
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.ModifiedResult
import core.domain.exception.IllegalModificationError
import core.domain.order.model.Order
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.shipping.model.ProductsShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.model.ValidationResult

/**
 * Represents the aggregate root of the whole data model
 */
interface BasketData {

    /**
     * Add a new [BasketItem] to the [BasketData]. Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun addBasketItem(product: Product, price: Price): BasketData

    /**
     * Add a new [BasketItem] to the [BasketData] and recalculates its items.
     * Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun addBasketItemAndRecalculate(
        product: Product, price: Price, checkoutData: CheckoutData,
        shippingCostService: ShippingCostService,
        basketCalculationService: BasketCalculationService,
    ): BasketData

    /**
     * Removes a [BasketItem] from the [BasketData]. Calls [validateIfModificationIsAllowed] beforehand.
     */
    fun removeBasketItem(basketItemId: BasketItemId): BasketData

    /**
     * Cancels the [BasketData] hindering all future changes to it. Only applicable if the [BasketData] is not [BasketStatus.FROZEN]
     */
    fun cancel(): BasketData

    /**
     * Puts the [BasketData] in [BasketStatus.FINALIZED]. Only applicable if the [BasketData] is in [BasketStatus.FROZEN]
     */
    fun finalize(): BasketData

    /**
     * Returns true if the [Price] is older than a set threshold
     */
    fun requiresPriceRefresh(): Boolean

    /**
     * Refreshes all [Price]s of the [BasketItem]s
     * @return [ModifiedResult.Refreshed] if a [Price] was refreshed, else [ModifiedResult.Unchanged]
     */
    fun refreshPrices(pricePort: PricePort): ModifiedResult<BasketData>

    /**
     * Returns true if the [Product] is older than a set threshold
     */
    fun requiresProductRefresh(): Boolean

    /**
     * Refreshes all [Product]s of the [BasketItem]s
     * @return [ModifiedResult.Refreshed] if a [Product] was refreshed, else [ModifiedResult.Unchanged]
     */
    fun refreshProducts(productPort: ProductPort): ModifiedResult<BasketData>

    /**
     * Resets the [BasketData]. Sets the basket to [BasketStatus.OPEN].
     * The basket has to be [BasketStatus.FROZEN]
     */
    fun unfreeze(): BasketData

    /**
     * Validates the basket and all its content.
     * @return [ValidationResult]
     */
    fun validate(): ValidationResult

    /**
     * Validates if any changes are allowed to be made to the basket depending on the [BasketStatus]
     * @throws [IllegalModificationError] if changes to the basket data are currently not allowed
     */
    fun validateIfModificationIsAllowed()

    fun setOrder(order: Order): BasketData

    /**
     * Returns a list of [BasketItem] with the same [ProductId] as the [BasketItem] of the passed [BasketItemId]
     */
    @JsonIgnore
    fun getBasketItemsWithSameProduct(basketItemId: BasketItemId): List<BasketItem>

    @JsonIgnore
    fun getBasketId(): BasketId
    fun getOutletId(): OutletId
    fun getStatus(): BasketStatus
    fun getItems(): List<BasketItem>
    fun canBeModified(): Boolean
    fun getOrder(): Order?

    @JsonIgnore
    fun getProductIdList(): List<ProductId>

    @JsonIgnore
    fun isOpen(): Boolean

    @JsonIgnore
    fun isFrozen(): Boolean
    fun freeze(): BasketData
    fun updateShippingCostAndRecalculateBasket(
        shippingCosts: ProductsShippingCost,
        basketCalculationService: BasketCalculationService,
    ): ModifiedResult<Unit>

    fun removeBasketItemAndRecalculate(basketItemId: BasketItemId, basketCalculationService: BasketCalculationService): BasketDataAggregate
}
