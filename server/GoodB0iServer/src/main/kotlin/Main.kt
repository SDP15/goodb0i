import controller.lists
import controller.shelves
import controller.sockets.sockets
import controller.products
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.getProperty
import repository.DataProvider
import repository.DatabaseFactory
import repository.exposedTypeAdapters
import service.*
import service.routing.IntRouteFinder
import service.shopping.AppManager
import service.shopping.SessionManager
import service.shopping.TrolleyManager
import java.time.Duration


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging) // Log all calls
    install(WebSockets)

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




    environment.config.apply {
        val root = System.getProperty("user.dir")
        val productsPath = propertyOrNull("ktor.resources.products")?.getString() ?:"$root/src/main/resources/products.json"
        val racksPath = propertyOrNull("ktor.resources.racks")?.getString() ?: "$root/src/main/resources/racks.json"
        val graphPath =propertyOrNull("ktor.resources.graph")?.getString() ?:"$root/src/main/resources/graph.json"
        val listsPath = propertyOrNull("ktor.resources.lists")?.getString() ?: "$root/src/main/resources/lists.json"


        val graph = DataProvider.loadFromFile(productsPath, racksPath, graphPath, listsPath)

        val routeFinder = IntRouteFinder(graph)
        val sessionManager = SessionManager(listService, routeFinder)
        val trolleyManager = TrolleyManager()
        val appManager = AppManager(sessionManager)


        install(Routing) {
            products(productService)
            shelves(shelfService)
            lists(listService)
            sockets(sessionManager, trolleyManager, appManager)
        }

    }


}


class Main {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            println("Command line args ${args.map { it }}")
            val clEnv = commandLineEnvironment(args)

//            val env = applicationEngineEnvironment {
//                config = clEnv.application.environment.config
//                module(Application::module)
//            }
            embeddedServer(Netty, clEnv).start()
            //embeddedServer(Netty, port = 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
        }
    }
}

