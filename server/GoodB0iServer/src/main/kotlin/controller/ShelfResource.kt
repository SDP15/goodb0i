package controller

import io.ktor.routing.Route
import io.ktor.routing.route
import service.ShelfService

fun Route.shelves(shelfService: ShelfService) {

    route("/shelves") {


    }
}