package controller

import com.google.gson.internal.LinkedTreeMap
import io.ktor.application.call
import io.ktor.features.toLogString
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import service.ListService
import java.util.*

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
                    Pair(UUID.fromString(it.values.elementAt(0) as String), // UUID string
                            (it.values.elementAt(1) as Double).toInt())  // Quantity
                }
                println("Items are $flat")
                val response = listService.createList(flat)
                when (response) {
                    is ListService.ListServiceResponse.ListResponse -> {
                        call.respondText(response.list.code.toString(), status = HttpStatusCode.Created)
                    }
                    is ListService.ListServiceResponse.ListServiceError.ProductsNotFound -> {
                        call.respond(HttpStatusCode.BadRequest, response.products)
                    }
                }
            }
        }

        post("/update/{code}") {
            val code = call.parameters["code"]?.toLongOrNull()
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val ids = call.receiveOrNull<List<LinkedTreeMap<String, Any>>>()
                if (ids == null) {
                    call.respond(HttpStatusCode.BadRequest, "Post a list of pairs of UUID and quantity")
                } else {
                    val flat = ids.map {
                        Pair(UUID.fromString(it.values.elementAt(0) as String), // UUID string
                                (it.values.elementAt(1) as Double).toInt())  // Quantity
                    }
                    val response = listService.createList(flat)
                    when (response) {
                        is ListService.ListServiceResponse.ListResponse -> {
                            call.respondText(response.list.code.toString(), status = HttpStatusCode.Created)
                        }
                        is ListService.ListServiceResponse.ListServiceError.ProductsNotFound -> {
                            call.respond(HttpStatusCode.BadRequest, response.products)
                        }
                    }
                }
            }
        }

        get("/load/{code}") {
            val code = call.parameters["code"]?.toLongOrNull()
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val response = listService.loadList(code)
                when (response) {
                    is ListService.ListServiceResponse.ListResponse -> {
                        call.respond(response.list)
                    }
                    is ListService.ListServiceResponse.ListServiceError.ListNotFound -> {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }

    }

}