package service

import model.Stocks
import model.ChangeType
import model.Notification
import model.Stock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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

    suspend fun getAllStock(): List<Stock> = transaction {
        Stock.all().toList()
    }

    suspend fun getStock(id: Int): Stock? = dbQuery {
        Stock.findById(id)
    }

<<<<<<< HEAD
    suspend fun search(query: String?): List<Stock> = dbQuery {
        AllStock.selectAll().filter {
            (it[AllStock.name] + it[AllStock.description] + it[AllStock.department] + it[AllStock.superDepartment]).toLowerCase().contains(query?.toLowerCase() ?: "")
        }.map(AllStock::toStock)
    }

    suspend fun updateStock(stock: Stock): Stock? {
        val id = stock.id
        return if (id == -1) { //TODO: Better way of doing this
            addStock(stock)
        } else {
            dbQuery {
=======
>>>>>>> exposed_setup

    suspend fun search(query: String?): List<Stock> = transaction {
        Stock.all().filter {
            (it.name + it.description + it.department).toLowerCase().contains(query?.toLowerCase() ?: "")
        }
    }

//    suspend fun updateStock(stock: Stock): Stock? {
//
//    }
//
//    suspend fun addStock(stock: Stock): Stock {
//    }

    suspend fun deleteStock(id: Int): Boolean = dbQuery {
        Stocks.deleteWhere { Stocks.id eq id } > 0
    }
}


