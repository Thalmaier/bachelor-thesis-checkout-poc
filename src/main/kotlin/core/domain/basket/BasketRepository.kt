package core.domain.basket

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.common.Port
import core.domain.price.model.Price
import core.domain.product.model.Product

/**
 * Repository for handling access to the database for a [Basket] entity
 */
@Port
interface BasketRepository {

    /**
     * Returns a stale version of the [Basket].
     * Data like [Price] or [Product] will not be updated.
     */
    fun getStaleBasket(id: BasketId): Basket

    /**
     * Returns an updated version of the [Basket].
     */
    fun getRefreshedBasket(id: BasketId): Basket

    /**
     * Stores the [Basket] in the database
     */
    fun save(basket: Basket)

}
