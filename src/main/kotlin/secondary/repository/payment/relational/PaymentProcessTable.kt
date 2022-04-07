package secondary.repository.payment.relational

import core.domain.payment.model.PaymentMethod
import core.domain.payment.model.PaymentProcessStatus
import core.domain.payment.model.PaymentStatus
import org.jetbrains.exposed.sql.Table
import secondary.repository.common.relational.money

object PaymentProcessTable : Table() {
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
    val amountPaid = money("amountPaid")
    val amountToPay = money("amountToPay")
    val amountToReturn = money("amountToReturn")
    val status = enumeration("status", PaymentProcessStatus::class)
    val externalPaymentRef = uuid("externalPaymentRef").nullable()
}

object PaymentTable : Table() {
    val basketId = uuid("basketId").index()
    val id = uuid("id").uniqueIndex().autoGenerate()
    override val primaryKey = PrimaryKey(id, name = "PK_PaymentTable")
    val method = enumeration("method", PaymentMethod::class)
    val amountSelected = money("amountSelected")
    val amountUsed = money("amountUsed")
    val amountOverpaid = money("amountOverpaid")
    val status = enumeration("status", PaymentStatus::class)
}