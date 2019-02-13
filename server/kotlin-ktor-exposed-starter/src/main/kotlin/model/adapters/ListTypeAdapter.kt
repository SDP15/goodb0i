package model.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import model.List
import org.jetbrains.exposed.sql.transactions.transaction

object ListTypeAdapter : TypeAdapter<List>() {
    override fun write(out: JsonWriter, list: List) {
        transaction {
            out.beginObject()
            out.name("code")
            out.value(list.code)
            out.name("time")
            out.value(list.time)
            out.name("products")

            out.beginArray()
            println("Writing products to JSON ${list.products.map { it.product.name }}")
            list.products.forEach{ listProduct ->
                println("Writing product with quantity ${listProduct.quantity}")
                out.beginObject()
                out.name("quantity")
                out.value(listProduct.quantity)
                out.name("product")
                StockTypeAdapter.write(out, listProduct.product)
                out.endObject()
            }
            out.endArray()
            out.endObject()
        }

    }

    override fun read(`in`: JsonReader?): List {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}