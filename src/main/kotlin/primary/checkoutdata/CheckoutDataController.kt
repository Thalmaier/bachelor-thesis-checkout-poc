package primary.checkoutdata;

import core.application.checkoutdata.CheckoutDataApiPort
import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.Customer
import core.domain.checkoutdata.model.CheckoutData
import core.domain.payment.model.Payment
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [CheckoutData] resources.
 */
@PrimaryAdapter
class CheckoutDataController(
    checkoutDataApiPort: CheckoutDataApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        /**
         * GET endpoint to retrieve a [BasketData] resource
         */
        get("/basket/{id}/checkout") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for basket $basketId" }

            val aggregates = checkoutDataApiPort.findCheckoutDataById(basketId)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * PUT endpoint to set the current [Customer] of an existing [BasketData]
         */
        put("/basket/{id}/checkout/customer") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val customer = call.receive<Customer>()
            logger.info { "Received request to set customer data for basket $basketId" }

            val aggregates = checkoutDataApiPort.setCustomer(basketId, customer)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * PUT endpoint to set the current [FulfillmentType] of an existing [BasketData]
         */
        put("/basket/{id}/checkout/fulfillment") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val fulfillmentType = call.receive<FulfillmentType>()
            logger.info { "Received request to set fulfillment $fulfillmentType to basket $basketId" }

            val aggregates = checkoutDataApiPort.setFulfillment(basketId, fulfillmentType)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * PUT endpoint to set the current [ShippingAddress][Address] of an existing [BasketData]
         */
        put("/basket/{id}/checkout/shipping-address") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val shippingAddress = call.receive<Address>()
            logger.info { "Received request to set shipping address to basket $basketId" }

            val aggregates = checkoutDataApiPort.setShippingAddress(basketId, shippingAddress)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * PUT endpoint to set the current [BillingAddress][Address] of an existing [BasketData]
         */
        put("/basket/{id}/checkout/billing-address") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val billingAddress = call.receive<Address>()
            logger.info { "Received request to set billing address to basket $basketId" }

            val aggregates = checkoutDataApiPort.setBillingAddress(basketId, billingAddress)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * PUT endpoint to set all checkout data at once of an existing [BasketData].
         * This method sets [FulfillmentType], [ShippingAddress][Address], [BillingAddress][Address],
         * [Customer] and [Payment] if they are not null in the passed [SetCheckoutDataApiRequest].
         */
        put("/basket/{id}/checkout") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val checkoutDataRequest = call.receive<SetCheckoutDataApiRequest>()
            logger.info { "Received request to set checkout data to basket $basketId" }

            val aggregates = checkoutDataApiPort.setCheckoutData(
                basketId, checkoutDataRequest.fulfillment, checkoutDataRequest.shippingAddress,
                checkoutDataRequest.billingAddress, checkoutDataRequest.customer, checkoutDataRequest.payment
            )

            call.respond(HttpStatusCode.OK, aggregates)
        }
    }
}
