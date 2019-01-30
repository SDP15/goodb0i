import com.fasterxml.jackson.databind.SerializationFeature
import com.google.gson.Gson
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.launch
import model.Stock
import service.DatabaseFactory
import service.StockService
import web.stock
import java.io.File


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    DatabaseFactory.init()

    val stockService = StockService()
    launch {
        println("Stock insert running")
        getTestData().forEach {
            println("Inserting $it")
            stockService.addStock(it)
        }
        print("Stock insert finished")
    }


    install(Routing) {
        stock(stockService)
    }

}


private fun getTestData(): Array<Stock> {
    val path = System.getProperty("user.dir") + "/src/main/resources/items.json"
    val file = File(path).bufferedReader()
    val gson = Gson()
    return gson.fromJson(file, Array<Stock>::class.java)
}

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
            val wd = System.getProperty("user.dir")
            println("Working directory $wd")
        }
    }
}

