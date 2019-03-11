import controller.lists
import controller.shelves
import controller.sockets.sockets
import controller.products
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import repository.DatabaseFactory
import repository.TestDataProvider
import repository.exposedTypeAdapters
import service.*
import service.routing.Graph
import service.routing.RouteFinder
import service.shopping.AppManager
import service.shopping.SessionManager
import service.shopping.TrolleyManager
import java.util.concurrent.TimeUnit


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging) // Log all calls
    install(WebSockets) // Enable WebSockets

    // Automatic conversion according to ContentType headers
    install(ContentNegotiation) {
        gson(block = exposedTypeAdapters())
    }

    install(Compression) {
        gzip()
    }


    DatabaseFactory.init()

    val productService = ProductService()
    val shelfService = ShelfService()
    val listService = ListService()

    TestDataProvider.insert()


    val routeFinder = RouteFinder(listService,
            Graph.graph<Int> {
                // Test shelves are 3, 1, 5, 7
                // 1         2         3              4         5       6         7         8
                //"Dairy", "Bakery", "Fruits", "Vegetables", "Seafood", "Meat", "Sweets", "Food cupboard"
                10 to 3 cost 5 // Start to fruits
                3 to 11 cost 5  // Fruits to top left
                11 to 12 cost 5 // Top left to top right
                11 to 1 cost 5 // Top left to dairy
                1 to 5 cost 5 // dairy to seafood
                5 to 12 cost 5// Seafood to top right
                12 to 7 cost 5// Top right to sweets
                7 to 13 cost 5// Sweets to end

            })
    val sessionManager = SessionManager(routeFinder)
    val trolleyManager = TrolleyManager()
    val appManager = AppManager(sessionManager)


    install(Routing) {
        products(productService)
        shelves(shelfService)
        lists(listService)
        sockets(sessionManager, trolleyManager, appManager)
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

