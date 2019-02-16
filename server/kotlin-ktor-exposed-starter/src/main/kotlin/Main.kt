import controller.lists
import controller.shelves
import controller.sockets.sockets
import controller.products
import controller.sockets.SocketSession
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.websocket.WebSockets
import repository.DatabaseFactory
import repository.TestDataProvider
import repository.lists.ShoppingList
import repository.products.Product
import repository.adapters.ListTypeAdapter
import repository.adapters.ProductTypeAdapter
import service.*
import service.shopping.AppManager
import service.shopping.TrolleyManager


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging) // Log all calls
    install(WebSockets) // Enable WebSockets

    // Automatic conversion according to ContentType headers
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            registerTypeAdapter(Product::class.java, ProductTypeAdapter)
            registerTypeAdapter(ShoppingList::class.java, ListTypeAdapter)
        }
    }

    DatabaseFactory.init()

    val productService = ProductService()
    val shelfService = ShelfService()
    val listService = ListService()

    TestDataProvider.insert()

    install(Routing) {
        products(productService)
        shelves(shelfService)
        lists(listService)
        sockets(TrolleyManager(), AppManager())
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

