package controller

import com.google.gson.internal.LinkedTreeMap
import io.ktor.application.call
import io.ktor.features.toLogString
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
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
                //TODO: If this works the same as the implementation of ProductResourceTest,
                // asking for an Array rather than a List may parse correctly (not a treemap)
                //TODO: Error handling
                val flat = ids.map {
                    Pair(it.values.elementAt(0) as String, // UUID string
                            (it.values.elementAt(1) as Double).toInt())  // Quantity
                }
                println("Items are $flat")
                val list = listService.createList(flat.map { it.first }, flat.map { it.second })
                if (list != null) {
                    call.respondText(list.code.toString(), status = HttpStatusCode.Created)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        get("/load/{code}") {
            val code = call.parameters["code"]?.toLongOrNull()
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val list = listService.loadList(code)
                if (list != null) {
                    call.respond(list)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }

    }

}