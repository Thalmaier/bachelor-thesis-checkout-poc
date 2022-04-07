package secondary.adapter.shipping

import config.Config
import core.domain.basketdata.model.FulfillmentType
import mu.KotlinLogging
import org.javamoney.moneta.Money
import secondary.adapter.MockTimeoutService

/**
 * Calls used to make api request to the shipping api
 */
class ShippingApiService {

    private val logger = KotlinLogging.logger {}

    /**
     * Returns a [ShippingCostApiResponse] for [ShippingInformation]
     * This method returns dummy data, because we dont want any dependencies on external systems for a POC.
     */
    fun fetchShippingCosts(shippingInformation: ShippingInformation): ShippingCostApiResponse? {
        logger.info { "Fetch shipping cost for $shippingInformation from the external system" }
        MockTimeoutService.timeout(25, "fetchShippingCosts")
        return ShippingCostApiResponse(
            shippingCosts = shippingInformation.items.associateWith {
                // Just random calculation method
                when (shippingInformation.fulfillmentType) {
                    FulfillmentType.PICKUP -> Money.zero(Config().currency.currencyUnit)
                    else -> Money.of(shippingInformation.items.size * 1.5, Config().currency.currencyUnit)
                }
            }
        )
    }

}
