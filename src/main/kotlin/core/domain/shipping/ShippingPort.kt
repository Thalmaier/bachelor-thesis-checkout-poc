package core.domain.shipping

import core.domain.basketdata.model.BasketData
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.Port
import core.domain.shipping.model.ProductsShippingCost

/**
 * Port used for communication with the shipping adapter
 */
@Port
interface ShippingPort {

    /**
     * Determines the shipping cost of basket
     */
    fun determineShippingCosts(basketData: BasketData, checkoutData: CheckoutData): ProductsShippingCost

}
