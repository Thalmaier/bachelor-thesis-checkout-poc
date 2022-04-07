package core.domain.shipping.model

import core.domain.product.model.ProductId

/**
 * Maps the [ProductId] to their corresponding [ShippingCost]
 */
typealias ProductsShippingCost = Map<ProductId, ShippingCost>
