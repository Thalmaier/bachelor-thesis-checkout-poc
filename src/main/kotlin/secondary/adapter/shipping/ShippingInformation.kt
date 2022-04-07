package secondary.adapter.shipping

import core.domain.basket.model.Address
import core.domain.basket.model.Basket
import core.domain.basket.model.FulfillmentType
import core.domain.product.model.ProductId

/**
 * DTO for calling [ShippingApiService.fetchShippingCosts]
 */
data class ShippingInformation(
    val fulfillmentType: FulfillmentType,
    val shippingAddress: Address?,
    val items: List<ProductId>,
) {
    constructor(basket: Basket) : this(
        basket.getFulfillment(),
        basket.getShippingAddress(),
        basket.getProductIdList()
    )
}
