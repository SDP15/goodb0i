import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.launch
import model.List
import model.Stock
import model.adapters.StockTypeAdapter
import org.jetbrains.exposed.sql.transactions.transaction
import service.DatabaseFactory
import service.ListService
import service.ShelfService
import service.StockService
import web.lists
import web.shelves
import web.stock
import java.io.File


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(Stock::class.java, StockTypeAdapter)
            registerTypeAdapter(List::class.java, object : TypeAdapter<model.List>() {
                override fun write(out: JsonWriter, list: List) {
                    transaction {
                        out.beginObject()
                        out.name("code")
                        out.value(list.code)
                        out.name("time")
                        out.value(list.time)
                        out.name("products")

                        out.beginObject()
                        println("Writing products to JSON ${list.products.map { it.product.name }}")
                        list.products.forEachIndexed { index, listProduct ->
                            println("Writing product with quantity ${listProduct.quantity}")
                            out.name(index.toString())
                            out.beginObject()
                            out.name("quantity")
                            out.value(listProduct.quantity)
                            out.name("product")
                            StockTypeAdapter.write(out, listProduct.product)
                            out.endObject()
                        }
                        out.endObject()
                        out.endObject()
                    }

                }

                override fun read(`in`: JsonReader?): List {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
        }
//        jackson {
//            configure(SerializationFeature.INDENT_OUTPUT, true)
//        }
    }

    DatabaseFactory.init()

    val stockService = StockService()
    val shelfService = ShelfService()
    launch {
        println("Stock insert running")
        transaction {
            getTestData().forEach {
                Stock.new {
                    name = it.name
                    averageSellingUnitWeight = it.averageSellingUnitWeight
                    contentsMeasureType = it.contentsMeasureType
                    contentsQuantity = it.contentsQuantity
                    unitOfSale = it.unitOfSale
                    unitQuantity = it.unitQuantity
                    department = it.department
                    description = it.description.joinToString("//")
                    price = it.price
                    superDepartment = it.superDepartment
                    unitPrice = it.unitPrice
                }
            }
            print("Attempting read ${Stock.all().toList()}")
        }

        print("Stock insert finished")

        shelfService.initDefaultShelves()
        transaction {
            ListService().createList(Stock.all().limit(4).toList().map { it.id.value.toString() }, listOf(5, 4, 3, 2))
        }
    }


    install(Routing) {
        stock(stockService)
        shelves(shelfService)
        lists(ListService())
    }

}


data class Item(
        @SerializedName("id") val id: Long = -1,
        @SerializedName("name") val name: String,
        @SerializedName("AverageSellingUnitWeight") val averageSellingUnitWeight: Double,
        @SerializedName("ContentsMeasureType") val contentsMeasureType: String,
        @SerializedName("contentsQuantity") val contentsQuantity: Double,
        @SerializedName("UnitOfSale") val unitOfSale: Int,
        @SerializedName("UnitQuantity") val unitQuantity: String,
        @SerializedName("department") val department: String,
        @SerializedName("description") val description: kotlin.collections.List<String>,
        @SerializedName("price") val price: Double,
        @SerializedName("superDepartment") val superDepartment: String,
        @SerializedName("unitPrice") val unitPrice: Double


)

private fun getTestData(): Array<Item> {

    val path = System.getProperty("user.dir") + "/src/main/resources/items.json"
    val file = File(path).bufferedReader()

    val gson = Gson()
    return gson.fromJson(file, Array<Item>::class.java)
}

class Main {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, port = 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
            val wd = System.getProperty("user.dir")
            println("Working directory $wd")
        }
    }
}

