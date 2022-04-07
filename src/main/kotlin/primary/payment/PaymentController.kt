package primary.payment

import core.domain.basket.model.Basket
import core.domain.basket.model.BasketId
import core.domain.basket.model.BasketStatus
import core.domain.payment.model.Payment
import core.domain.payment.model.PaymentId
import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcess
import core.domain.payment.service.PaymentApiPort
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import primary.PrimaryAdapter
import primary.common.parseUUIDFromParameter

/**
 * Controller defining the endpoints to access and manipulate the [PaymentProcess] and [Payment] resources.
 */
@PrimaryAdapter
class PaymentController(paymentApiPort: PaymentApiPort) {

    private val logger = KotlinLogging.logger {}

    val route: Route.() -> Unit = {
        route("/basket/{basketId}/payment") {
            /**
             * GET endpoint to retrieve a list of all available [PaymentMethod] for a [Basket]
             */
            get("/available-payment-methods") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received retrieval request for all available payment methods for basket $basketId" }

                val availablePayments = paymentApiPort.getAvailablePaymentMethods(basketId)
                call.respond(HttpStatusCode.OK, availablePayments)
            }
            /**
             * POST endpoint for creation of a new [Payment]
             */
            post {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val addPaymentApiRequest = call.receive<AddPaymentApiRequest>()
                logger.info { "Received request to add payment to basket $basketId" }

                val basket = paymentApiPort.addPaymentToBasket(basketId, addPaymentApiRequest.toPayment())
                call.respond(HttpStatusCode.OK, basket)
            }
            /**
             * DELETE endpoint to cancel an existing [Payment]. [Payment]s cannot be deleted, rather just disabled.
             */
            delete("/{paymentId}") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                val paymentId = PaymentId(parseUUIDFromParameter("paymentId"))
                logger.info { "Received request to cancel payment $paymentId from basket $basketId" }

                val basket = paymentApiPort.cancelPayment(basketId, paymentId)
                call.respond(HttpStatusCode.OK, basket)
            }
            /**
             * POST endpoint to initialize a [PaymentProcess]
             */
            post("/initialize") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to initialize payment on basket $basketId" }

                val basket = paymentApiPort.initializePaymentProcessAndFreezeBasket(basketId)
                call.respond(HttpStatusCode.OK, basket)
            }
            /**
             * POST endpoint to execute a [PaymentProcess]. The [PaymentProcess] needs to be initialized first.
             * Put the [Basket] into [BasketStatus.FINALIZED]
             */
            post("/execute") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to execute payment on basket $basketId" }

                val basket = paymentApiPort.executePaymentProcessAndFinalizeBasket(basketId)
                call.respond(HttpStatusCode.OK, basket)
            }
            /**
             * POST endpoint to cancel a [PaymentProcess]. The [PaymentProcess] needs to be initialized first.
             */
            delete("/cancel") {
                val basketId = BasketId(parseUUIDFromParameter("basketId"))
                logger.info { "Received request to cancel payment on basket $basketId" }

                val basket = paymentApiPort.cancelPaymentProcessAndResetBasket(basketId)
                call.respond(HttpStatusCode.OK, basket)
            }
        }
    }

}
