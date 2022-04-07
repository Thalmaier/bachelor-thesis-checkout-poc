package primary.basket

import core.application.basket.BasketItemApiPort
import core.domain.basket.model.BasketId
import core.domain.basket.model.BasketItem
import core.domain.basket.model.BasketItemId
import core.domain.product.model.ProductId
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.BadRequestError
import primary.common.fromParameter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [BasketItem] resources.
 */
@PrimaryAdapter
class BasketItemController(
    basketItemApiPort: BasketItemApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        route("/basket/{basketId}/item") {
            /**
             * POST endpoint for creation of new [BasketItem] corresponding to a [ProductId]
             */
            post("{productId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val productId = ProductId(parseUUIDFromParameter("productId"))
                logger.info { "Received request to add product $productId to basket $basketId" }

                val basket = basketItemApiPort.addBasketItem(basketId, productId)
                context.respond(HttpStatusCode.OK, basket)
            }
            /**
             * DELETE endpoint for cancellation of an existing [BasketItem]
             */
            delete("{basketItemId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val basketItemId = BasketItemId(parseUUIDFromParameter("basketItemId"))
                logger.info { "Received request to remove basket item $basketItemId from basket $basketId" }

                val basket = basketItemApiPort.removeBasketItem(basketId, basketItemId)
                context.respond(HttpStatusCode.OK, basket)
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

                val basket = basketItemApiPort.setBasketItemQuantity(basketId, basketItemId, quantity)
                context.respond(HttpStatusCode.OK, basket)
            }
        }
    }
}
