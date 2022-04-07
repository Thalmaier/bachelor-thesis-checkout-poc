package secondary.adapter.price

import core.domain.price.PricePort
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.adapter.SecondaryAdapter
import secondary.repository.price.PriceCache
import secondary.repository.price.document.CachedPriceMongoRepository

/**
 * Adapter for a [PricePort], which stores the result in a [CachedPriceMongoRepository]
 */
@SecondaryAdapter
class CachedPriceAdapter(
    priceApiService: PriceApiService,
    private val priceRepository: PriceCache,
) : PriceAdapter(priceApiService) {

    override fun fetchPrice(priceId: PriceId): Price {
        return priceRepository.getAndUpdateIfInvalid(priceId, fallback = {
            super.fetchPrice(priceId)
        })
    }

}
