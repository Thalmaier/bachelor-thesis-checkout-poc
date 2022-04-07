package secondary.repository.payment.relational

import core.domain.basketdata.model.BasketId
import core.domain.payment.PaymentProcessRepository
import core.domain.payment.model.*
import org.jetbrains.exposed.sql.ResultRow
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.metricInsertOrUpdate
import secondary.repository.common.relational.metricSelect

class PaymentProcessPostgreRepository : PaymentProcessRepository, PostgresRepository(
    tables = arrayOf(PaymentProcessTable, PaymentTable)
) {
    override fun findPaymentProcess(id: BasketId): PaymentProcess {
        val dao = PaymentProcessTable.metricSelect { PaymentProcessTable.id eq id.id }.singleOrNull()
        return dao?.let { mapPaymentProcess(it) } ?: PaymentProcessAggregate(id)
    }

    override fun save(paymentProcess: PaymentProcess) {
        PaymentProcessTable.metricInsertOrUpdate(PaymentProcessTable.id) {
            it[id] = paymentProcess.getBasketId().id
            it[amountPaid] = paymentProcess.getAmountPaid()
            it[amountToPay] = paymentProcess.getAmountToPay()
            it[amountToReturn] = paymentProcess.getAmountToReturn()
            it[status] = paymentProcess.getStatus()
            it[externalPaymentRef] = paymentProcess.getExternalPaymentRef()?.id
        }
        paymentProcess.getPayments().forEach { payment ->
            PaymentTable.metricInsertOrUpdate(PaymentTable.id) {
                it[basketId] = paymentProcess.getBasketId().id
                it[id] = payment.id.id
                it[method] = payment.method
                it[amountSelected] = payment.amountSelected
                it[amountUsed] = payment.amountUsed
                it[amountOverpaid] = payment.amountOverpaid
                it[status] = payment.status
            }
        }
    }

    private fun mapPaymentProcess(dao: ResultRow): PaymentProcess {
        return PaymentProcessAggregate(
            id = BasketId(dao[PaymentProcessTable.id]),
            payments = mapPayments(BasketId(dao[PaymentProcessTable.id])),
            amountToPay = dao[PaymentProcessTable.amountToPay],
            amountToReturn = dao[PaymentProcessTable.amountToReturn],
            amountPaid = dao[PaymentProcessTable.amountPaid],
            status = dao[PaymentProcessTable.status],
            _externalPaymentRef = dao[PaymentProcessTable.externalPaymentRef]?.let { ExternalPaymentRef(it) }
        )
    }

    private fun mapPayments(id: BasketId): MutableSet<Payment> {
        val set = mutableSetOf<Payment>()
        PaymentTable.metricSelect { PaymentTable.basketId eq id.id }.forEach { res ->
            set.add(
                Payment(
                    id = PaymentId(res[PaymentTable.id]),
                    method = res[PaymentTable.method],
                    amountSelected = res[PaymentTable.amountSelected],
                    _amountOverpaid = res[PaymentTable.amountOverpaid],
                    _amountUsed = res[PaymentTable.amountUsed],
                    _status = res[PaymentTable.status]
                )
            )
        }
        return set
    }

}