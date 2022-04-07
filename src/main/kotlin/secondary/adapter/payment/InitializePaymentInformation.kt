package secondary.adapter.payment

import core.domain.payment.model.ExternalPaymentRef
import core.domain.payment.model.Payment

/**
 * DTO for calling [PaymentAdapter.initializeAllSubPayments]
 */

data class InitializePaymentInformation(
    val externalPaymentRef: ExternalPaymentRef,
    val payments: Set<Payment>,
)
