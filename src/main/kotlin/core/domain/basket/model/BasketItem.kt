package core.domain.basket.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import core.domain.calculation.CalculationResultFactory
import core.domain.calculation.model.BasketItemCalculationResult
import core.domain.common.Entity
import core.domain.common.ModifiedResult
import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.product.ProductPort
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.shipping.model.ShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.ValidationResult

/**
 * One item of a [Basket] containing a [Product] and additional information
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class BasketItem(
    val id: BasketItemId,
    private var product: Product,
    private var price: Price,
    private var shippingCost: ShippingCost = ShippingCostService.ZERO_MONEY,
    private var calculationResult: BasketItemCalculationResult = BasketItemCalculationResult(),
) : Entity(id) {

    /**
     * Calculates the [BasketItemCalculationResult] and [ShippingCost]
     */
    fun calculate(shippingCost: ShippingCost): ModifiedResult<BasketItem> {
        val newCalculationResult = CalculationResultFactory.basketItemResult(price, product.vat, shippingCost)
        return when (calculationResult != newCalculationResult) {
            true -> ModifiedResult.Updated(
                this.apply {
                    this.calculationResult = newCalculationResult
                    this.shippingCost = shippingCost
                }
            )
            false -> ModifiedResult.Unchanged(this)
        }
    }

    /**
     * Validates if the item and its [BasketItemCalculationResult] is up-to-date
     */
    fun validate(parent: String, result: ValidationResult) {
        val node = "$parent.basketItem.${id.id}"
        result.addResults(
            invalidIf(
                node, "calculationResult.itemCost.grossAmount",
                price.grossAmount != calculationResult.itemCost.grossAmount,
                "product gross amount differs from calculation item gross amount"
            ),
            invalidIf(
                node, "calculationResult.itemCost.vatAmounts",
                product.vat.rate != calculationResult.itemCost.vatAmounts[product.vat.sign]?.rate,
                "product vat rate is not contained in the vat Amounts in calculation"
            ),
            invalidIf(
                node, "calculationResult.shippingCost",
                shippingCost != calculationResult.shippingCost,
                "item shipping cost differ from calculated shipping cost"
            )
        )
        price.validate(node, result)
        product.validate(node, result)
    }

    /**
     * Returns true if the [Price] needs to be refreshed because it could be not valid anymore
     */
    fun requiresPriceRefresh(): Boolean = price.requiresRefresh()

    /**
     * Refreshes the [Price] of the [BasketItem]
     */
    fun refreshPrice(pricePort: PricePort): ModifiedResult<BasketItem> {
        val newPrice = pricePort.fetchPrice(price.id)
        return ModifiedResult.Refreshed(this.apply { this.price = newPrice })
    }

    /**
     * Returns true if the [Product] needs to be refreshed because it could be not valid anymore
     */
    fun requiresProductRefresh(): Boolean = product.requiresRefresh()

    /**
     * Refreshes the [Product] of the [BasketItem]
     */
    fun refreshProduct(productPort: ProductPort): ModifiedResult<BasketItem> {
        val newProduct = productPort.fetchProduct(product.id)
        return ModifiedResult.Refreshed(this.apply { this.product = newProduct })
    }

    fun getCalculationResult(): BasketItemCalculationResult = this.calculationResult
    fun getProductId(): ProductId = this.product.id
    fun getProduct(): Product = this.product
    fun getPrice(): Price = this.price
    fun getShippingCost(): ShippingCost = this.shippingCost

}
