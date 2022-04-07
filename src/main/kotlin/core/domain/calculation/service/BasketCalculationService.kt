package core.domain.calculation.service

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.calculation.model.BasketCalculation
import core.domain.checkoutdata.model.CheckoutData
import core.domain.payment.model.PaymentProcess

interface BasketCalculationService {

    fun recalculateIfNecessaryAndSave(
        basketId: BasketId,
        basketData: BasketData? = null,
        checkoutData: CheckoutData? = null,
        paymentProcess: PaymentProcess? = null,
    ): BasketCalculation

    fun getUpdatedCalculation(id: BasketId): BasketCalculation
}
