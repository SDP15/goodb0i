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

    val trolleyManager = TrolleyManager()
    val appManager = AppManager()
    val routeFinder = RouteFinder(listService)
    val sessionManager = SessionManager(routeFinder)


    install(Routing) {
        products(productService)
        shelves(shelfService)
        lists(listService)
        sockets(sessionManager, trolleyManager, appManager)
    }
    GlobalScope.launch {
        TimeUnit.SECONDS.sleep(5)
        routeFinder.plan(7654321)
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

