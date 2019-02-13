package web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.websocket.webSocket
import model.Stock
import service.StockService

fun Route.stock(stockService: StockService) {

    route("/stock") {

        get("/") {
            val stock = stockService.getAllStock()
            println("Router retrieved stock $stock")
            stock.forEach {

                println("Stock item ${it}")
            }
            call.respond(stock)
        }

        get("/search/{query}" ) {
            println("Query ${call.parameters["query"]}")

            val stock = stockService.search(call.parameters["query"])
            if (stock.isEmpty()) call.respond(HttpStatusCode.NotFound)
            else call.respond(stock)
        }

        get("/{id}") {
            val stock = stockService.getStock(call.parameters["id"]?.toInt()!!)
            if (stock == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(stock)
        }

        get("/search/{query}") {
            val items = stockService.search(call.parameters["query"])
            if (items.isEmpty()) call.respond(HttpStatusCode.NotFound)
            else call.respond(items)
        }

        post("/") {
            val stock = call.receive<Stock>()
            call.respond(HttpStatusCode.Created, /*stockService.addStock(stock)*/"")
        }

        put("/") {
            val stock = call.receive<Stock>()
            val updated = null // stockService.updateStock(stock)
            if (updated == null) call.respond(HttpStatusCode.NotFound)
            //else call.respond(HttpStatusCode.OK, updated)
        }

//        delete("/{id}") {
//            val id = call.parameters["id"]?.toInt()!!
//            val removed = stockService.deleteStock(id)
//            if (removed) call.respond(HttpStatusCode.OK)
//            else call.respond(HttpStatusCode.NotFound)
//        }

    }

    val mapper = jacksonObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)

    }

    webSocket("/updates") {
        try {
            stockService.addChangeListener(this.hashCode()) {
                outgoing.send(Frame.Text(mapper.writeValueAsString(it)))

            }
            while (true) {
                incoming.receiveOrNull() ?: break
            }
        } finally {
            stockService.removeChangeListener(this.hashCode())
        }
    }
}
