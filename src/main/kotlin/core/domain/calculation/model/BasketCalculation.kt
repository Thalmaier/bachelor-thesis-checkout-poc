package core.domain.calculation.model

import com.fasterxml.jackson.annotation.JsonIgnore
import core.domain.basketdata.model.BasketId
import core.domain.shipping.model.ShippingCost
import core.domain.validation.model.ValidationResult
import javax.money.MonetaryAmount

interface BasketCalculation {
    fun getGrandTotal(): MonetaryAmount
    fun getNetTotal(): MonetaryAmount
    fun getVatAmounts(): Map<Int, VatAmount>
    fun getShippingCostTotal(): ShippingCost
    @JsonIgnore fun getBasketId(): BasketId
    fun validate(): ValidationResult
}
