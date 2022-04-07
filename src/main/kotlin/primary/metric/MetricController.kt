package primary.metric

import core.application.metric.Metric
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import primary.PrimaryAdapter

@PrimaryAdapter
class MetricController(
    metric: Metric = Metric,
) {

    val route: Route.() -> Unit = {
        get("/metric") {
            call.respond(HttpStatusCode.OK, metric.getMetrics())
        }
    }
}