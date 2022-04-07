package secondary.adapter.shipping

import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.FulfillmentType
import core.domain.checkoutdata.model.CheckoutData
import core.domain.product.model.ProductId

/**
 * DTO for calling [ShippingApiService.fetchShippingCosts]
 */
data class ShippingInformation(
    val fulfillmentType: FulfillmentType,
    val shippingAddress: Address?,
    val items: List<ProductId>,
) {
    constructor(basketData: BasketData, checkoutData: CheckoutData) : this(
        checkoutData.getFulfillment(),
        checkoutData.getShippingAddress(),
        basketData.getProductIdList()
    )
}
