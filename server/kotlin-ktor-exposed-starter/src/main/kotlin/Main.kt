import com.fasterxml.jackson.databind.SerializationFeature
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
import org.litote.kmongo.async.KMongo
import org.litote.kmongo.async.findOne
import org.litote.kmongo.async.getCollection
import org.litote.kmongo.eq
import service.DatabaseFactory
import service.StockService
import web.stock

import org.litote.kmongo.*

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

    install(Routing) {
        stock(stockService)
    }

}

data class Jedi(val name: String, val age: Int)

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("test") //normal java driver usage
    val col = database.getCollection<Jedi>() //KMongo extension method
//here the name of the collection by convention is "jedi"
//you can use getCollection<Jedi>("otherjedi") if the collection name is different
    col.insertOne(Jedi("Luke Skywalker", 19)) {_, _ -> }

}