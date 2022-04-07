package core.domain.shipping

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketItem
import core.domain.common.Port
import core.domain.shipping.model.ProductsShippingCost

/**
 * Port used for communication with the shipping adapter
 */
@Port
interface ShippingPort {

    /**
     * Determines the shipping cost of a whole [Basket] per [BasketItem]
     */
    fun determineShippingCosts(basket: Basket): ProductsShippingCost

}
