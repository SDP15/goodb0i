import model.AllShelfRacks
import model.AllStock
import model.Stock
import org.jetbrains.exposed.sql.Table

object AllShelf : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val product = (integer("stock_id") references AllStock.id)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rackId = (integer("rackId") references AllShelfRacks.id)

}

data class Shelf(val id: Int, val product: Stock, val quantity: Int)