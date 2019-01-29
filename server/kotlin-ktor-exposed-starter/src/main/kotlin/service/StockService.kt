package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery

class StockService {

    private val listeners = mutableMapOf<Int, suspend (Notification<Stock?>) -> Unit>()

    fun addChangeListener(id: Int, listener: suspend (Notification<Stock?>) -> Unit) {
        listeners[id] = listener
    }

    fun removeChangeListener(id: Int) = listeners.remove(id)

    private suspend fun onChange(type: ChangeType, id: Int, entity: Stock? = null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    suspend fun getAllStock(): List<Stock> = dbQuery {
        AllStock.selectAll().map { toStock(it) }
    }

    suspend fun getStock(id: Int): Stock? = dbQuery {
        AllStock.select {
            (AllStock.id eq id)
        }.mapNotNull { toStock(it) }
                .singleOrNull()
    }

    suspend fun updateStock(stock: NewStock): Stock? {
        val id = stock.id
        return if (id == null) {
            addStock(stock)
        } else {
            dbQuery {

                AllStock.update({ AllStock.id eq id }) {
                    it[name] = stock.name
                    it[averageSellingUnitWeight] = stock.averageSellingUnitWeight
                    it[contentsMeasureType] = stock.contentsMeasureType
                    it[contentsQuantity] = stock.contentsQuantity
                    it[unitOfSale] = stock.unitOfSale
                    it[unitQuantity] = stock.unitQuantity
                    it[department] = stock.department
                    it[description] = stock.description.joinToString("//")
                    it[price]= stock.price
                    it[superDepartment] = stock.superDepartment
                    it[unitprice] = stock.unitprice

                }
            }
            getStock(id).also {
                onChange(ChangeType.UPDATE, id, it)
            }
        }
    }

    suspend fun addStock(stock: NewStock): Stock {
        var key = 0
        dbQuery {
            key = (AllStock.insert {
                it[name] = stock.name
            } get AllStock.id)!!
        }
        return getStock(key)!!.also {
            onChange(ChangeType.CREATE, key, it)
        }
    }

    suspend fun deleteStock(id: Int): Boolean {
        return dbQuery {
            AllStock.deleteWhere { AllStock.id eq id } > 0
        }.also {
            if (it) onChange(ChangeType.DELETE, id)
        }
    }

    private fun toStock(row: ResultRow): Stock =
            Stock(
                    id = row[AllStock.id],
                    name = row[AllStock.name]
            )

}
