import model.AllStock
import model.ShelfRack
import model.Stock
import org.jetbrains.exposed.sql.Table

object AllShelf : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val product = (integer("stock_id") references AllStock.id)
    val quantity = (integer("quantity"))
    val position = (integer("position"))
    val rackId = (integer("rackId") references ShelfRack.id)

}

data class Shelf(val id: Int, val product: Stock, val quantity: Int)