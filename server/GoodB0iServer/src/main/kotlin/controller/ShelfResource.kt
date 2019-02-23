package controller

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import service.ShelfService

fun Route.shelves(shelfService: ShelfService) {

    route("/shelves") {

        get("/{id}") {
            val products = shelfService.getProductForShelfRack(call.parameters["id"]!!.toInt())
            call.respond(if (products.isNotEmpty()) products else HttpStatusCode.NotFound)
        }

    }
}