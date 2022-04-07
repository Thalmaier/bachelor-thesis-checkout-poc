package primary.payment

import core.domain.basketdata.model.BasketData
import core.domain.basketdata.model.BasketId
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentProcessAggregate
import core.domain.payment.service.PaymentProcessApiPort
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [PaymentProcessAggregate] and [Payment] resources.
 */
@PrimaryAdapter
class PaymentController(
    paymentApiPort: PaymentProcessApiPort,
) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        route("/basket/{basketId}/payment") {
            get {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received retrieval request for payment process for basket $basketId" }

                val aggregates = paymentApiPort.getPaymentProcess(basketId)
                call.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * POST endpoint for creation of a new [Payment]
             */
            post {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val addPaymentApiRequest = call.receive<AddPaymentApiRequest>()
                logger.info { "Received request to add payment to basket $basketId" }

                val aggregates = paymentApiPort.addPayment(basketId, addPaymentApiRequest.toPayment())
                call.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * DELETE endpoint to cancel an existing [Payment]. [Payment]s cannot be deleted, rather just disabled.
             */
            delete("/{paymentId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val paymentId = PaymentId(parseUUIDFromParameter("paymentId"))
                logger.info { "Received request to cancel payment $paymentId from basket $basketId" }

                val aggregates = paymentApiPort.cancelPayment(basketId, paymentId)
                call.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * GET endpoint to retrieve a list of all available payments for a [BasketData]
             */
            get("/available-payment-methods") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received retrieval request for all available payment methods for basket $basketId" }

                val availablePayments = paymentApiPort.getAvailablePaymentMethods(basketId)
                call.respond(HttpStatusCode.OK, availablePayments)
            }
            /**
             * POST endpoint to initialize a [PaymentProcessAggregate]
             */
            post("/initialize") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to initialize payment on basket $basketId" }

                val aggregates = paymentApiPort.initializePaymentProcessAndFreezeBasket(basketId)
                call.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * POST endpoint to execute a [PaymentProcessAggregate]. The [PaymentProcessAggregate] needs to be initialized first.
             */
            post("/execute") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to execute payment on basket $basketId" }

                val aggregates = paymentApiPort.executePaymentProcessAndFinalizeBasket(basketId)
                call.respond(HttpStatusCode.OK, aggregates)
            }
            /**
             * POST endpoint to cancel a [PaymentProcessAggregate]. The [PaymentProcessAggregate] needs to be initialized first.
             */
            post("/cancel") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to cancel payment on basket $basketId" }

                val aggregates = paymentApiPort.cancelPaymentProcessAndResetBasket(basketId)
                call.respond(HttpStatusCode.OK, aggregates)
            }
        }
    }

}
