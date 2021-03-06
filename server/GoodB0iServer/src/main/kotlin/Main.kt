
import controller.lists
import controller.products
import controller.shelves
import controller.sockets.sockets
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.routing.Routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import org.koin.ktor.ext.installKoin
import repository.DataProvider
import repository.DatabaseFactory
import repository.exposedTypeAdapters
import service.routing.IntRouteFinder
import service.routing.RouteFinder
import service.shopping.AppManager
import service.shopping.SessionManager
import service.shopping.TrolleyManager
import java.time.Duration


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging) // Log all calls
    install(WebSockets) {
        timeout = Duration.ofSeconds(60)
    }

    // Automatic conversion according to ContentType headers
    install(ContentNegotiation) {
        gson(block = exposedTypeAdapters())
    }

    install(Compression) {
        gzip()
    }

    DatabaseFactory.init()





    environment.config.apply {
        val root = System.getProperty("user.dir")
        val productsPath = propertyOrNull("ktor.resources.products")?.getString() ?:"$root/src/main/resources/products.json"
        val racksPath = propertyOrNull("ktor.resources.racks")?.getString() ?: "$root/src/main/resources/racks.json"
        val graphPath =propertyOrNull("ktor.resources.graph")?.getString() ?:"$root/src/main/resources/graph.json"
        val listsPath = propertyOrNull("ktor.resources.lists")?.getString() ?: "$root/src/main/resources/lists.json"


        val graph = DataProvider.loadFromFile(productsPath, racksPath, graphPath, listsPath)

        installKoin(listOf(
                ServiceModule,
                org.koin.dsl.module.module {
                    single<RouteFinder> { IntRouteFinder(graph)}
                }
        ))

        val sessionManager = SessionManager()
        val trolleyManager = TrolleyManager()
        val appManager = AppManager(sessionManager)

        install(Routing) {
            products()
            shelves()
            lists()
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

