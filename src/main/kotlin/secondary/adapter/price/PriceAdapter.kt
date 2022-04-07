package secondary.adapter.price

import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.adapter.SecondaryAdapter
import secondary.adapter.exception.FetchingExternalResourceError

/**
 * Adapter for a [PricePort]
 */
@SecondaryAdapter
open class PriceAdapter(private val priceApiService: PriceApiService) : PricePort {

    override fun fetchPrice(priceId: PriceId): Price {
        return priceApiService.fetchPrice(priceId)
            ?.toPrice()
            ?: throw FetchingExternalResourceError("Could not fetch price for product for id $priceId")
    }

}
