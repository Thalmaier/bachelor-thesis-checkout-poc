package core.application.basket

import core.domain.basket.model.*
import core.domain.common.Port
import core.domain.exception.IllegalModificationError
import core.domain.exception.ResourceNotFoundError
import core.domain.product.model.Product
import core.domain.product.model.ProductId

/**
 * Port for handling all basket item api request
 */
@Port
interface BasketItemApiPort {

    /**
     * Creates a new [BasketItem] on a [Basket] containing the [Product] for the passed [ProductId].
     * The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the [Basket]
     * @throws [ResourceNotFoundError] if the [Basket] or [Product] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun addBasketItem(basketId: BasketId, productId: ProductId): Basket

    /**
     * Removes a [BasketItem] on a [Basket].
     * The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [Basket]
     * @param basketItemId [BasketItemId] of the [BasketItem], which should be removed
     * @throws [ResourceNotFoundError] if the [Basket] or [BasketItem] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun removeBasketItem(basketId: BasketId, basketItemId: BasketItemId): Basket

    /**
     * Sets the quantity for a [BasketItem] on a [Basket].
     * The [Basket] needs to be in status [BasketStatus.OPEN]
     * @param basketId [BasketId] of the to be canceled [Basket]
     * @param basketItemId [BasketItemId] of the [BasketItem], which should be removed
     * @throws [ResourceNotFoundError] if the [Basket] or [BasketItem] does not exist
     * @throws [IllegalModificationError] if the [Basket] is not [BasketStatus.OPEN]
     */
    fun setBasketItemQuantity(basketId: BasketId, basketItemId: BasketItemId, quantity: Int): Basket

}
