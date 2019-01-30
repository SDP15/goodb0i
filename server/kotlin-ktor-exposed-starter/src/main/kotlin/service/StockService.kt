package service

import model.Stocks
import model.ChangeType
import model.Notification
import model.Stock
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
        Stock.all().toList()
    }

    suspend fun getStock(id: Int): Stock? = dbQuery {
            Stock.findById(id)
    }

    suspend fun updateStock(stock: Stock): Stock? {

    }

    suspend fun addStock(stock: Stock): Stock {
        Stock.new {  }
    }

    suspend fun deleteStock(id: Int): Boolean {
        return dbQuery {
            Stocks.deleteWhere { Stocks.id eq id } > 0
        }.also {
            if (it) onChange(ChangeType.DELETE, id)
        }
    }

}
