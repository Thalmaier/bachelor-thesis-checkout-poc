package secondary.adapter.product

import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.product.model.Vat

/**
 * DTO for transporting the response of the [ProductApiService]
 */
data class ProductApiResponse(
    val id: ProductId,
    val name: String,
    val vat: Vat,
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            vat = vat
        )
    }
}
