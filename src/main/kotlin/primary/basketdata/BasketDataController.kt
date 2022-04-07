package primary.basketdata

import core.application.basketdata.BasketDataApiPort
import core.application.basketdata.BasketDataItemApiPort
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.BasketItem
import core.domain.basketdata.model.BasketItemId
import core.domain.product.model.ProductId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.common.BadRequestError
import primary.common.fromParameter
import primary.common.parseUUIDFromParameter

class BasketDataController(
    basketDataApiPort: BasketDataApiPort,
    basketDataItemApiPort: BasketDataItemApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        /**
         * GET endpoint to retrieve a [BasketData] resource
         */
        get("/basket/{id}") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for basket $basketId" }

            val aggregates = basketDataApiPort.findBasketDataById(basketId)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * POST endpoint for creation of a new [BasketData]
         */
        post("/basket") {
            val creationRequest = call.receive<BasketCreationApiRequest>()
            logger.info { "Received request to create a basket with outletId ${creationRequest.outletId}" }

            val aggregates = basketDataApiPort.createBasket(creationRequest.outletId, creationRequest.customer)
            call.respond(HttpStatusCode.Created, aggregates)
        }
        /**
         * DELETE endpoint for canceling an existing [BasketData]
         */
        delete("/basket/{id}") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received request to cancel basket $basketId" }

            val aggregates = basketDataApiPort.cancelBasket(basketId)
            call.respond(HttpStatusCode.OK, aggregates)
        }
        /**
         * GET endpoint retrieve a [BasketData] resource
         */
        get("/basket/{id}/available-fulfillment") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for available fulfillment to basket $basketId" }

            val fulfillment = basketDataApiPort.getAvailableFulfillment(basketId)
            call.respond(HttpStatusCode.OK, fulfillment)
        }

        route("/basket/{basketId}/data/item") {
            /**
             * POST endpoint for creation of new [BasketItem] corresponding to the passed [ProductId]
             */
            post("{productId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val productId = ProductId(parseUUIDFromParameter("productId"))
                logger.info { "Received request to add product $productId to basket $basketId" }

                val aggregates = basketDataItemApiPort.addBasketItem(basketId, productId)
                context.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * DELETE endpoint for deletion of an existing [BasketItem]
             */
            delete("{basketItemId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val basketItemId = BasketItemId(parseUUIDFromParameter("basketItemId"))
                logger.info { "Received request to remove basket item $basketItemId from basket $basketId" }

                val aggregates = basketDataItemApiPort.removeBasketItem(basketId, basketItemId)
                context.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * PUT endpoint for adjusting the quantity of a [BasketItem]
             */
            put("{basketItemId}/quantity") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val basketItemId = BasketItemId(parseUUIDFromParameter("basketItemId"))
                val quantity: Int = fromParameter("quantity").toIntOrNull()
                    ?: throw BadRequestError("quantity parameter is not an number")
                logger.info { "Received request to set quantity of basket item $basketItemId for basket $basketId to $quantity" }

                val aggregates = basketDataItemApiPort.setBasketItemQuantity(basketId, basketItemId, quantity)
                context.respond(HttpStatusCode.OK, aggregates)
            }
        }

    }

}
