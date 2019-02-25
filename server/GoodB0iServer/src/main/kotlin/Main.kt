import controller.lists
import controller.shelves
import controller.sockets.sockets
import controller.products
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
import repository.DatabaseFactory
import repository.TestDataProvider
import repository.exposedTypeAdapters
import service.*
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

    DatabaseFactory.init()

    val productService = ProductService()
    val shelfService = ShelfService()
    val listService = ListService()

    TestDataProvider.insert()

    val trolleyManager = TrolleyManager()
    val appManager = AppManager()
    val sessionManager = SessionManager()

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

