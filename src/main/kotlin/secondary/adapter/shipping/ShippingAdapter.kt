package secondary.adapter.shipping

import core.domain.basket.model.Basket
import core.domain.shipping.ShippingPort
import core.domain.shipping.model.ProductsShippingCost
import secondary.adapter.SecondaryAdapter
import secondary.adapter.exception.FetchingExternalResourceError

/**
 * Adapter for a [ShippingPort]
 */
@SecondaryAdapter
class ShippingAdapter(private val shippingApiService: ShippingApiService) : ShippingPort {

    override fun determineShippingCosts(basket: Basket): ProductsShippingCost {
        val shippingInformation = ShippingInformation(basket)
        return shippingInformation.shippingAddress?.let {
            shippingApiService.fetchShippingCosts(shippingInformation)
                ?.shippingCosts
                ?: throw FetchingExternalResourceError("Could not fetch shipping costs")
        } ?: emptyMap()
    }
}
