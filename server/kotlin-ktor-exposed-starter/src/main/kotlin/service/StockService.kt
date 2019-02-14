package service

import model.Stock
import model.Stocks
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StockService {

    fun getAllStock(): List<Stock> = transaction {
        Stock.all().toList()
    }

    fun getStock(id: String): Stock? = transaction {
        Stock.findById(UUID.fromString(id))
    }

    fun search(query: String?): List<Stock> = transaction {
        Stock.all().filter {
            (it.name + it.description + it.department).toLowerCase().contains(query?.toLowerCase()
                    ?: "")
        }
    }

    fun deleteStock(id: UUID): Boolean = transaction {
        Stocks.deleteWhere { Stocks.id eq id } > 0
    }
}


