package controller

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import service.ProductService

fun Route.products() {
    val productService: ProductService by inject()
    route("/products") {

        get("/") {
            val products = productService.getAllProducts()
            println("Router retrieved products $products")
            call.respond(products)
        }

        get("/search/{query}") {
            println("Query ${call.parameters["query"]}")
            val products = productService.search(call.parameters["query"])
            call.respond(if (products.isNotEmpty()) products else HttpStatusCode.NotFound)
        }

        get("/{id}") {
            val product = productService.getProduct(call.parameters["id"]!!)
            call.respond(product ?: HttpStatusCode.NotFound)
        }

        get("/barcode/{barcode}") {
            val product = productService.getProductByBarcode(call.parameters["barcode"]!!)
            call.respond(product ?: HttpStatusCode.NotFound)
        }

    }
}
