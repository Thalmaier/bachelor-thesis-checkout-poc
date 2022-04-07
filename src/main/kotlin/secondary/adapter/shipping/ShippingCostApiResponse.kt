package secondary.adapter.shipping

import core.domain.product.model.ProductId
import javax.money.MonetaryAmount

/**
 * DTO for transporting the response of the [ShippingApiService]
 */
data class ShippingCostApiResponse(
    val shippingCosts: Map<ProductId, MonetaryAmount>,
)
