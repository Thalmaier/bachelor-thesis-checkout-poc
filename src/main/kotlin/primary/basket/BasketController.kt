package primary.basket;

import core.application.basket.BasketApiPort
import core.domain.basket.model.Address
import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.basket.model.FulfillmentType
import core.domain.basket.model.customer.Customer
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
 * Controller defining the endpoints to access and manipulate the [Basket] resources.
 */
@PrimaryAdapter
class BasketController(
    basketApiPort: BasketApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        /**
         * GET endpoint to retrieve a [Basket] resource
         */
        get("/basket/{id}") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for basket $basketId" }

            val basket = basketApiPort.findBasketById(basketId)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * POST endpoint for creation of a new [Basket]
         */
        post("/basket") {
            val creationRequest = call.receive<BasketCreationApiRequest>()
            logger.info { "Received request to create a basket with outletId ${creationRequest.outletId}" }

            val basket = basketApiPort.createBasket(creationRequest.outletId, creationRequest.customer)
            call.respond(HttpStatusCode.Created, basket)
        }
        /**
         * DELETE endpoint for canceling an existing [Basket]
         */
        delete("/basket/{id}") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received request to cancel basket $basketId" }

            val basket = basketApiPort.cancelBasket(basketId)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * PUT endpoint to set a [Customer] of an existing [Basket]
         */
        put("/basket/{id}/customer") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val customer = call.receive<Customer>()
            logger.info { "Received request to set customer data for basket $basketId" }

            val basket = basketApiPort.setCustomer(basketId, customer)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * GET endpoint for fetching all available [FulfillmentType]s for a basket
         */
        get("/basket/{id}/available-fulfillment") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for available fulfillment to basket $basketId" }

            val fulfillment = basketApiPort.getAvailableFulfillment(basketId)
            call.respond(HttpStatusCode.OK, fulfillment)
        }
        /**
         * PUT endpoint to set the current [FulfillmentType] of an existing [Basket]
         */
        put("/basket/{id}/fulfillment") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val fulfillmentType = call.receive<FulfillmentType>()
            logger.info { "Received request to set fulfillment $fulfillmentType to basket $basketId" }

            val basket = basketApiPort.setFulfillment(basketId, fulfillmentType)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * PUT endpoint to set the current [ShippingAddress][Address] of an existing [Basket]
         */
        put("/basket/{id}/shipping-address") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val shippingAddress = call.receive<Address>()
            logger.info { "Received request to set shipping address to basket $basketId" }

            val basket = basketApiPort.setShippingAddress(basketId, shippingAddress)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * PUT endpoint to set the current [BillingAddress][Address] of an existing [Basket]
         */
        put("/basket/{id}/billing-address") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val billingAddress = call.receive<Address>()
            logger.info { "Received request to set billing address to basket $basketId" }

            val basket = basketApiPort.setBillingAddress(basketId, billingAddress)
            call.respond(HttpStatusCode.OK, basket)
        }
        /**
         * PUT endpoint to set all checkout data at once of an existing [Basket].
         * This method sets [FulfillmentType], [ShippingAddress][Address], [BillingAddress][Address],
         * [Customer] and [Payment] if they are not null in a [SetCheckoutDataApiRequest].
         */
        put("/basket/{id}/checkout-data") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            val checkoutData = call.receive<SetCheckoutDataApiRequest>()
            logger.info { "Received request to set checkout data to basket $basketId" }

            val basket = basketApiPort.setCheckoutData(
                basketId, checkoutData.fulfillment, checkoutData.shippingAddress,
                checkoutData.billingAddress, checkoutData.customer, checkoutData.payment
            )

            call.respond(HttpStatusCode.OK, basket)
        }
    }
}
