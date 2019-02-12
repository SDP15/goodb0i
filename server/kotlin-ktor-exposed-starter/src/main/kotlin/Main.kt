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
import model.Stock
import model.Stocks
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import service.DatabaseFactory
import service.ListService
import service.ShelfService
import service.StockService
import web.shelves
import web.stock
import java.io.File


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(Stock::class.java, object: TypeAdapter<Stock>() {
                override fun write(out: JsonWriter, value: Stock) {
                    out.beginObject()
                    out.name("id")
                    out.value(value.id.value.toString())
                    out.name("name")
                    out.value(value.name)
                    out.name("superDepartment")
                    out.value(value.superDepartment)
                    out.name("ContentsMeasureType")
                    out.value(value.contentsMeasureType)
                    out.name("UnitOfSale")
                    out.value(value.unitOfSale)
                    out.name("description")
                    out.beginArray()
                    value.description.split("//").forEach {
                        out.value(it)
                    }
                    out.endArray()
                    out.name("AverageSellingUnitWeight")
                    out.value(value.averageSellingUnitWeight)
                    out.name("UnitQuantity")
                    out.value(value.unitQuantity)
                    out.name("contentsQuantity")
                    out.value(value.contentsQuantity)
                    out.name("department")
                    out.value(value.department)
                    out.name("price")
                    out.value(value.price)
                    out.name("unitPrice")
                    out.value(value.unitPrice)
                    out.endObject()
                }

                override fun read(`in`: JsonReader?): Stock {
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
            ListService().createList(Stock.all().limit(4).toList().map { it.id.value.toString()}, listOf(5, 4, 3, 2))
        }
    }


    install(Routing) {
        stock(stockService)
        shelves(shelfService)
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

        @JvmStatic fun main(args: Array<String>) {
            embeddedServer(Netty, port=8080, watchPaths = listOf("MainKt"), module = Application::module).start()
            val wd = System.getProperty("user.dir")
            println("Working directory $wd")
        }
    }
}

