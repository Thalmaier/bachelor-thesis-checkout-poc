package secondary.adapter.shipping

import core.domain.basketdata.model.BasketData
import core.domain.checkoutdata.model.CheckoutData
import core.domain.shipping.ShippingPort
import core.domain.shipping.model.ProductsShippingCost
import secondary.adapter.SecondaryAdapter
import secondary.adapter.exception.FetchingExternalResourceError

/**
 * Adapter for a [ShippingPort]
 */
@SecondaryAdapter
class ShippingAdapter(
    private val shippingApiService: ShippingApiService,
) : ShippingPort {

    override fun determineShippingCosts(basketData: BasketData, checkoutData: CheckoutData): ProductsShippingCost {
        val shippingInformation = ShippingInformation(basketData, checkoutData)
        return shippingInformation.shippingAddress?.let {
            shippingApiService.fetchShippingCosts(shippingInformation)
                ?.shippingCosts
                ?: throw FetchingExternalResourceError("Could not fetch shipping costs")
        } ?: emptyMap()
    }
}
