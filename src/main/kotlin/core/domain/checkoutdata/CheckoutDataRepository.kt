package core.domain.checkoutdata

import core.domain.basketdata.model.BasketId
import core.domain.checkoutdata.model.CheckoutData
import core.domain.common.Port

/**
 * Repository for handling access to the database for a [CheckoutData] entity
 */
@Port
interface CheckoutDataRepository {

    /**
     * Returns a [CheckoutData] based on the [BasketId]
     */
    fun findCheckoutData(id: BasketId): CheckoutData

    /**
     * Stores the [CheckoutData] in the database
     */
    fun save(checkoutData: CheckoutData)

    fun resetOutdatedFlag(id: BasketId)
}
