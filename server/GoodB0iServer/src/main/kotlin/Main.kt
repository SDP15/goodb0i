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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import repository.DataProvider
import repository.DatabaseFactory
import repository.TestDataProvider
import repository.exposedTypeAdapters
import service.*
import service.routing.Graph
import service.routing.GenRouteFinder
import service.routing.IntRouteFinder
import service.shopping.AppManager
import service.shopping.SessionManager
import service.shopping.TrolleyManager


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


    val root = System.getProperty("user.dir")
    val productsPath =  "$root/src/main/resources/products.json"
    val racksPath = "$root/src/main/resources/racks.json"
    val graphPath = "$root/src/main/resources/graph.json"
    val listsPath = "$root/src/main/resources/lists.json"

    val graph = DataProvider.loadFromFile(productsPath, racksPath, graphPath, listsPath)

    val routeFinder = IntRouteFinder(listService,
            graph, start = 10, end = 13)
    val sessionManager = SessionManager(routeFinder)
    val trolleyManager = TrolleyManager()
    val appManager = AppManager(sessionManager)


    GlobalScope.launch {
        delay(3000)
        routeFinder.plan(7654321)
    }

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

