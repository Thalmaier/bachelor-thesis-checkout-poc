package secondary.repository.price

import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.repository.common.Cache

interface PriceCache : Cache<PriceId, Price>