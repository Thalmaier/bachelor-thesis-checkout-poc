package core.domain.checkoutdata

import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.customer.Customer
import core.domain.checkoutdata.model.CheckoutData
import core.domain.checkoutdata.model.CheckoutDataAggregate
import core.domain.common.Factory

/**
 * Factory for creating new [CheckoutData] instances.
 * This class is primarily more of an example.
 */
@Factory
class CheckoutDataFactory {
    fun createNewCheckoutData(id: BasketId, customer: Customer? = null): CheckoutData {
        return CheckoutDataAggregate(id = id, customer = customer)
    }
}
