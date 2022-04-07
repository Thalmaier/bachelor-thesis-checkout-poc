package core.domain.payment.model

import core.domain.common.Id
import java.util.*

/**
 * Value object for an reference to the external payment process
 */
data class ExternalPaymentRef(override val id: UUID = UUID.randomUUID()) : Id()
