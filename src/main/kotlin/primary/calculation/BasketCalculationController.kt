package primary.calculation;

import core.application.calculation.BasketCalculationApiPort
import core.domain.basketdata.model.BasketId
import core.domain.calculation.model.BasketCalculation
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [BasketCalculation] resources.
 */
@PrimaryAdapter
class BasketCalculationController(
    basketCalculationApiPort: BasketCalculationApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        /**
         * POST endpoint to update and retrieve a [BasketCalculation] resource
         */
        post("/basket/{id}/calculation") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for basket $basketId" }

            val basketCalculation = basketCalculationApiPort.findBasketCalculationById(basketId)
            call.respond(HttpStatusCode.OK, basketCalculation)
        }
    }
}
