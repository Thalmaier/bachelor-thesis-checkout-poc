package primary.aggregate;

import core.application.aggregate.AggregationApiPort
import core.domain.aggregate.Aggregates
import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [BasketData] resources.
 */
@PrimaryAdapter
class AggregateController(
    aggregationApiPort: AggregationApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        /**
         * GET endpoint to retrieve a complete [Aggregates] resource
         */
        get("/aggregate/{id}") {
            val basketId = BasketId(parseUUIDFromParameter("id"))
            logger.info { "Received retrieval request for basket $basketId" }

            val aggregates = aggregationApiPort.findBasketWithAllAggregates(basketId)
            call.respond(HttpStatusCode.OK, aggregates)
        }

    }
}
