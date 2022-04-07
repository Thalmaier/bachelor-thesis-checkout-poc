package secondary.repository.checkoutdata.relational

import core.domain.basketdata.model.Address
import core.domain.basketdata.model.BasketId
import core.domain.basketdata.model.customer.*
import core.domain.checkoutdata.CheckoutDataRepository
import core.domain.checkoutdata.model.CheckoutData
import core.domain.checkoutdata.model.CheckoutDataAggregate
import org.jetbrains.exposed.sql.ResultRow
import secondary.repository.common.relational.PostgresRepository
import secondary.repository.common.relational.metricInsertOrUpdate
import secondary.repository.common.relational.metricSelect

class CheckoutDataPostgreRepository : CheckoutDataRepository, PostgresRepository(
    tables = arrayOf(CheckoutDataTable)
) {
    override fun findCheckoutData(id: BasketId): CheckoutData {
        val dao = CheckoutDataTable.metricSelect { CheckoutDataTable.id eq id.id }.singleOrNull()
        return dao?.let { dao ->
            CheckoutDataAggregate(
                id = BasketId(dao[CheckoutDataTable.id]),
                fulfillmentType = dao[CheckoutDataTable.fulfillment],
                customer = mapCustomer(dao),
                billingAddress = mapBillingAddress(dao),
                shippingAddress = mapShippingAddress(dao),
            )
        } ?: CheckoutDataAggregate(id = id)
    }

    override fun save(checkoutData: CheckoutData) {
        CheckoutDataTable.metricInsertOrUpdate(CheckoutDataTable.id) {
            it[id] = checkoutData.getBasketId().id
            it[fulfillment] = checkoutData.getFulfillment()
            checkoutData.getCustomer()?.let { customer ->
                when (customer.type) {
                    CustomerType.SESSION_ID -> {
                        customer as SessionCustomer
                        it[customerType] = customer.type
                        it[customerSessionId] = customer.sessionId.id
                    }
                    CustomerType.IDENTIFIED -> {
                        customer as IdentifiedCustomer
                        it[customerType] = customer.type
                        it[customerBusinessType] = customer.businessType
                        it[customerCompanyName] = customer.companyName
                        it[customerCompanyTaxId] = customer.companyTaxId
                        it[customerCustomerTaxId] = customer.customerTaxId
                        it[customerEmail] = customer.email
                        it[customerNameFirstname] = customer.name.firstName
                        it[customerNameLastname] = customer.name.lastName
                    }
                }
            }
            checkoutData.getShippingAddress()?.let { shipping ->
                it[shippingAddressCountry] = shipping.country
                it[shippingAddressCity] = shipping.city
                it[shippingAddressZipCode] = shipping.zipCode
                it[shippingAddressStreet] = shipping.street
                it[shippingAddressHouseNumber] = shipping.houseNumber
            }
            checkoutData.getBillingAddress()?.let { billing ->
                it[billingAddressCountry] = billing.country
                it[billingAddressCity] = billing.city
                it[billingAddressZipCode] = billing.zipCode
                it[billingAddressStreet] = billing.street
                it[billingAddressHouseNumber] = billing.houseNumber
            }
        }
    }

    private fun mapCustomer(dao: ResultRow): Customer? {
        return when (dao[CheckoutDataTable.customerType]) {
            CustomerType.SESSION_ID -> SessionCustomer(
                sessionId = SessionId(dao[CheckoutDataTable.customerSessionId]!!)
            )
            CustomerType.IDENTIFIED -> IdentifiedCustomer(
                companyName = dao[CheckoutDataTable.customerCompanyName]!!,
                companyTaxId = dao[CheckoutDataTable.customerCompanyTaxId]!!,
                customerTaxId = dao[CheckoutDataTable.customerCustomerTaxId]!!,
                email = dao[CheckoutDataTable.customerEmail]!!,
                name = CustomerName(
                    firstName = dao[CheckoutDataTable.customerNameFirstname]!!,
                    lastName = dao[CheckoutDataTable.customerNameLastname]!!
                ),
                businessType = dao[CheckoutDataTable.customerBusinessType]!!
            )
            else -> null
        }
    }

    private fun mapShippingAddress(dao: ResultRow): Address? {
        if (dao[CheckoutDataTable.shippingAddressCity] == null) {
            return null
        }
        return Address(
            country = dao[CheckoutDataTable.shippingAddressCountry]!!,
            city = dao[CheckoutDataTable.shippingAddressCity]!!,
            zipCode = dao[CheckoutDataTable.shippingAddressZipCode]!!,
            street = dao[CheckoutDataTable.shippingAddressStreet]!!,
            houseNumber = dao[CheckoutDataTable.shippingAddressHouseNumber]!!
        )
    }

    private fun mapBillingAddress(dao: ResultRow): Address? {
        if (dao[CheckoutDataTable.billingAddressCountry] == null) {
            return null
        }
        return Address(
            country = dao[CheckoutDataTable.billingAddressCountry]!!,
            city = dao[CheckoutDataTable.billingAddressCity]!!,
            zipCode = dao[CheckoutDataTable.billingAddressZipCode]!!,
            street = dao[CheckoutDataTable.billingAddressStreet]!!,
            houseNumber = dao[CheckoutDataTable.billingAddressHouseNumber]!!
        )
    }

}