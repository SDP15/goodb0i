package web

import com.google.gson.internal.LinkedTreeMap
import io.ktor.application.call
import io.ktor.features.toLogString
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import service.ListService

fun Route.lists(listService: ListService) {

    route("/lists") {

        post("/new") {
            println("Call ${call.request.toLogString()}")
            val ids = call.receiveOrNull<List<LinkedTreeMap<String, Any>>>()
            if (ids == null) {
                call.respond(HttpStatusCode.BadRequest, "Post a list of pairs of UUID and quantity")
            } else {
                //TODO: Error handling
                val flat = ids.map {
                    Pair(it.values.elementAt(0) as String, // UUID string
                            (it.values.elementAt(1) as Double).toInt())  // Quantity
                }
                listService.createList(flat.map { it.first }, flat.map { it.second } )
                call.respond(HttpStatusCode.Created, "")
            }
        }

    }

}