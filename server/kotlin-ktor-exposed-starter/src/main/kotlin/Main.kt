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
import model.List
import model.Stock
import model.adapters.ListTypeAdapter
import model.adapters.StockTypeAdapter
import service.*
import web.app
import web.lists
import web.shelves
import web.stock


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            registerTypeAdapter(Stock::class.java, StockTypeAdapter)
            registerTypeAdapter(List::class.java, ListTypeAdapter)
        }
    }

    DatabaseFactory.init()

    val stockService = StockService()
    val shelfService = ShelfService()
    val listService = ListService()

    TestDataProvider.insert()

    install(Routing) {
        stock(stockService)
        shelves(shelfService)
        lists(listService)
        app()
    }

}




class Main {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, port = 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
        }
    }
}

