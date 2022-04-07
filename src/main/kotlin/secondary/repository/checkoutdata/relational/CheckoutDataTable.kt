package secondary.repository.checkoutdata.relational

import core.domain.basketdata.model.FulfillmentType
import core.domain.basketdata.model.customer.BusinessType
import core.domain.basketdata.model.customer.CustomerType
import org.jetbrains.exposed.sql.Table

object CheckoutDataTable : Table() {
    val id = uuid("id").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
    val outdated = bool("outdated")
    val fulfillment = enumeration("fulfillment", FulfillmentType::class)
    val customerType = enumeration("customerType", CustomerType::class).nullable()
    val customerSessionId = uuid("customerSessionId").nullable()
    val customerBusinessType = enumeration("customerBusinessType", BusinessType::class).nullable()
    val customerCompanyName = varchar("customerCompanyName", 50).nullable()
    val customerCompanyTaxId = varchar("customerCompanyTaxId", 50).nullable()
    val customerCustomerTaxId = varchar("customerCustomerTaxId", 50).nullable()
    val customerEmail = varchar("customerEmail", 50).nullable()
    val customerNameFirstname = varchar("customerNameFirstname", 50).nullable()
    val customerNameLastname = varchar("customerNameLastname", 50).nullable()
    val shippingAddressCountry = varchar("shippingAddressCountry", 50).nullable()
    val shippingAddressCity = varchar("shippingAddressCity", 50).nullable()
    val shippingAddressZipCode = varchar("shippingAddressZipCode", 50).nullable()
    val shippingAddressStreet = varchar("shippingAddressStreet", 50).nullable()
    val shippingAddressHouseNumber = varchar("shippingAddressHouseNumber", 50).nullable()
    val billingAddressCountry = varchar("billingAddressCountry", 50).nullable()
    val billingAddressCity = varchar("billingAddressCity", 50).nullable()
    val billingAddressZipCode = varchar("billingAddressZipCode", 50).nullable()
    val billingAddressStreet = varchar("billingAddressStreet", 50).nullable()
    val billingAddressHouseNumber = varchar("billingAddressHouseNumber", 50).nullable()

}