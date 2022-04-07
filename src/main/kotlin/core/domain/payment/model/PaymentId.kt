package core.domain.payment.model

import core.domain.common.Id
import java.util.*


/**
 * Value object for an id of a [Payment].
 */
data class PaymentId(override val id: UUID = UUID.randomUUID()) : Id()
