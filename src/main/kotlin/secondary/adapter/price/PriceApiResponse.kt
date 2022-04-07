package secondary.adapter.price

import core.domain.price.model.Price
import core.domain.price.model.PriceId
import secondary.common.TimeUtils
import javax.money.MonetaryAmount

/**
 * DTO for transporting the response of the [PriceApiService]
 */
data class PriceApiResponse(
    val id: PriceId,
    val grossAmount: MonetaryAmount,
) {
    fun toPrice(): Price {
        return Price(
            id = id,
            grossAmount = grossAmount,
            updatedAt = TimeUtils.dateTimeNow()
        )
    }
}
