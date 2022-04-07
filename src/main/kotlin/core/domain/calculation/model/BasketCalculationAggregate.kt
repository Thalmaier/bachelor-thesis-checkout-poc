package core.domain.calculation.model

import com.fasterxml.jackson.annotation.JsonAutoDetect
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.BasketItem
import core.domain.common.Entity
import core.domain.shipping.model.ShippingCost
import core.domain.shipping.service.ShippingCostService
import core.domain.validation.invalidIf
import core.domain.validation.model.ValidationResult
import org.bson.codecs.pojo.annotations.BsonId
import javax.money.MonetaryAmount

/**
 * Represents the result of a calculation of a [BasketData]
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
class BasketCalculationAggregate(
    @BsonId val id: BasketId,
    private var grandTotal: MonetaryAmount,
    private var netTotal: MonetaryAmount,
    private var shippingCostTotal: ShippingCost,
    private var vatAmounts: Map<Int, VatAmount>,
) : BasketCalculation, Entity(id) {

    constructor(id: BasketId) : this(
        id,
        ShippingCostService.ZERO_MONEY,
        ShippingCostService.ZERO_MONEY,
        ShippingCostService.ZERO_MONEY,
        emptyMap()
    )

    constructor(calculation: BasketCalculation) : this(
        calculation.getBasketId(), calculation.getGrandTotal(), calculation.getNetTotal(),
        calculation.getShippingCostTotal(), calculation.getVatAmounts()
    )

    constructor(basketData: BasketData) : this(
        basketData.getItems().map(BasketItem::getCalculationResult).combine().toBasketCalculationResult(basketData.getBasketId())
    )

    override fun validate(): ValidationResult {
        val node = "basketCalculation"
        return ValidationResult().apply {
            this.addResults(
                invalidIf(node, "grandTotal", grandTotal.isZero, "grandTotal should not be zero")
            )
        }
    }


    override fun getGrandTotal() = grandTotal
    override fun getNetTotal() = netTotal
    override fun getShippingCostTotal() = shippingCostTotal
    override fun getVatAmounts() = vatAmounts
    override fun getBasketId() = id

}
