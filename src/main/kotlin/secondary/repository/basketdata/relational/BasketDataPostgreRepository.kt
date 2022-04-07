package secondary.repository.basketdata.relational

import core.domain.basketdata.BasketDataRepository
import core.domain.basketdata.model.*
import core.domain.calculation.model.BasketItemCalculationResult
import core.domain.calculation.model.CalculationResult
import core.domain.order.model.Order
import core.domain.order.model.OrderRef
import core.domain.price.model.Price
import core.domain.price.model.PriceId
import core.domain.product.model.Product
import core.domain.product.model.ProductId
import core.domain.product.model.Vat
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import secondary.repository.common.relational.*
import java.math.BigDecimal
import java.util.*


class BasketDataPostgreRepository(
) : BasketDataRepository, PostgresRepository(
    tables = arrayOf(BasketDataTable, BasketItemTable, BasketItemItemCostVatTable, BasketItemTotalCostVatTable)
) {
    override fun findStaleBasketData(id: BasketId): BasketData {
        val dao = BasketDataTable.metricSelect { BasketDataTable.id eq id.id }.single()
        return mapBasketData(id, dao)
    }

    override fun save(basketData: BasketData) {
        BasketDataTable.metricInsertOrUpdate(BasketDataTable.id) {
            it[id] = basketData.getBasketId().id
            it[outletId] = basketData.getOutletId()
            it[status] = basketData.getStatus()
            it[orderId] = basketData.getOrder()?.orderRef?.id
            it[outdated] = basketData.getOutdated()
        }
        basketData.getItems().forEach { item ->
            BasketItemTable.metricInsertOrUpdate(BasketItemTable.id) {
                it[basketId] = basketData.getBasketId().id
                it[id] = item.id.id
                it[productId] = item.getProductId().id
                it[productName] = item.getProduct().name
                it[productVatRate] = item.getProduct().vat.rate.toDouble()
                it[productVatSign] = item.getProduct().vat.sign
                it[productUpdatedAt] = item.getProduct().updatedAt.toKotlinLocalDateTime()
                it[priceIdOutletId] = item.getPrice().id.outletId
                it[priceIdProductId] = item.getPrice().id.productId.id
                it[priceGrossAmount] = item.getPrice().grossAmount
                it[priceUpdatedAt] = item.getPrice().updatedAt.toKotlinLocalDateTime()
                it[shippingCost] = item.getShippingCost()
                it[itemCostGrossAmount] = item.getCalculationResult().itemCost.grossAmount
                it[itemCostNetAmount] = item.getCalculationResult().itemCost.netAmount
                it[calculationShippingCost] = item.getCalculationResult().shippingCost
                it[totalCostGrossAmount] = item.getCalculationResult().totalCost.grossAmount
                it[totalCostNetAmount] = item.getCalculationResult().totalCost.netAmount
            }
            item.getCalculationResult().totalCost.vatAmounts.values.forEach { vatAmount ->
                BasketItemTotalCostVatTable.metricInsertOrUpdate(BasketItemTotalCostVatTable.parentId, BasketItemTotalCostVatTable.sign) {
                    it[parentId] = item.id.id
                    it[sign] = vatAmount.sign
                    it[rate] = vatAmount.rate.toDouble()
                    it[amount] = vatAmount.amount
                }
            }
            item.getCalculationResult().itemCost.vatAmounts.values.forEach { vatAmount ->
                BasketItemItemCostVatTable.metricInsertOrUpdate(BasketItemItemCostVatTable.parentId, BasketItemItemCostVatTable.sign) {
                    it[parentId] = item.id.id
                    it[sign] = vatAmount.sign
                    it[rate] = vatAmount.rate.toDouble()
                    it[amount] = vatAmount.amount
                }
            }
        }
    }

    override fun resetOutdatedFlag(id: BasketId) {
        BasketDataTable.metricUpdate({
            BasketDataTable.id eq id.id
        }) {
            it[outdated] = false
        }
    }

    override fun resetOutdatedFlag(basketData: BasketData) {
        basketData.setOutdated(false)
        resetOutdatedFlag(basketData.getBasketId())
    }


    private fun mapBasketData(basketId: BasketId, dao: ResultRow): BasketData {
        return BasketDataAggregate(
            id = basketId,
            outletId = dao[BasketDataTable.outletId],
            status = dao[BasketDataTable.status],
            order = dao[BasketDataTable.orderId]?.let { Order(OrderRef(it)) },
            outdated = dao[BasketDataTable.outdated],
            items = mapBasketItems(basketId),
        )
    }

    private fun mapBasketItems(basketId: BasketId): MutableList<BasketItem> {
        val list = mutableListOf<BasketItem>()
        BasketItemTable.metricSelect { BasketItemTable.basketId eq basketId.id }.forEach { res ->
            list.add(
                BasketItem(
                    id = BasketItemId(res[BasketItemTable.id]),
                    product = Product(
                        id = ProductId(res[BasketItemTable.productId]),
                        name = res[BasketItemTable.productName],
                        vat = Vat(
                            sign = res[BasketItemTable.productVatSign],
                            rate = BigDecimal(res[BasketItemTable.productVatRate]),
                        ),
                        updatedAt = res[BasketItemTable.productUpdatedAt].toJavaLocalDateTime()
                    ),
                    price = Price(
                        id = PriceId(
                            outletId = res[BasketItemTable.priceIdOutletId],
                            productId = ProductId(res[BasketItemTable.priceIdProductId]),
                        ),
                        grossAmount = res[BasketItemTable.priceGrossAmount],
                        updatedAt = res[BasketItemTable.priceUpdatedAt].toJavaLocalDateTime(),
                    ),
                    shippingCost = res[BasketItemTable.shippingCost],
                    calculationResult = mapBasketItemCalculationResult(res, res[BasketItemTable.id])
                )
            )
        }
        return list
    }

    private fun mapBasketItemCalculationResult(res: ResultRow, id: UUID): BasketItemCalculationResult {
        return BasketItemCalculationResult(
            itemCost = CalculationResult(
                grossAmount = res[BasketItemTable.itemCostGrossAmount],
                netAmount = res[BasketItemTable.itemCostNetAmount],
                vatAmounts = mapVatAmounts(BasketItemItemCostVatTable, id),
            ),
            shippingCost = res[BasketItemTable.calculationShippingCost],
            totalCost = CalculationResult(
                grossAmount = res[BasketItemTable.totalCostGrossAmount],
                netAmount = res[BasketItemTable.totalCostNetAmount],
                vatAmounts = mapVatAmounts(BasketItemTotalCostVatTable, id),
            )
        )
    }

}
