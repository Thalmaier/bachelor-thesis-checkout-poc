package core.application.basketdata

import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.*
import core.domain.common.Port
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.product.model.Product
import core.domain.product.model.ProductId

/**
 * Port for handling all basket item api request
 */
@Port
interface BasketDataItemApiPort {

    /**
     * Creates a new [BasketItem] on a [BasketData] containing the [Product] for the passed [ProductId].
     * The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [BasketData]
     * @throws [ResourceNotFoundError] if the [BasketData] or [Product] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun addBasketItem(basketId: BasketId, productId: ProductId): Aggregates

    /**
     * Removes a [BasketItem] on a [BasketData].
     * The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [BasketData]
     * @param basketItemId [BasketItemId] of the [BasketItem], which should be removed
     * @throws [ResourceNotFoundError] if the [BasketData] or [BasketItem] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun removeBasketItem(basketId: BasketId, basketItemId: BasketItemId): Aggregates

    /**
     * Sets the quantity for a [BasketItem] on a [BasketData].
     * The [BasketData] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [BasketData]
     * @param basketItemId [BasketItemId] of the [BasketItem], which should be removed
     * @throws [ResourceNotFoundError] if the [BasketData] or [BasketItem] does not exist
     * @throws [IllegalModificationError] if the [BasketData] is not [BasketStatus.OPEN]
     */
    fun setBasketItemQuantity(basketId: BasketId, basketItemId: BasketItemId, quantity: Int): Aggregates

}
