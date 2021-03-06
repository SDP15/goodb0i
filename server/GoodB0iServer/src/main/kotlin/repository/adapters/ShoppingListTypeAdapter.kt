package repository.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.jetbrains.exposed.sql.transactions.transaction
import repository.lists.ShoppingList

object ShoppingListTypeAdapter : TypeAdapter<ShoppingList>() {
    override fun write(out: JsonWriter, shoppingList: ShoppingList) {
        transaction {
            out.beginObject()
            out.name("code")
            out.value(shoppingList.code)
            out.name("time")
            out.value(shoppingList.time)
            out.name("products")

            out.beginArray()
            println("Writing products to JSON ${shoppingList.products.map { it.product.name + "|" + it.product.id}}")
            // Order by the original user index here
            shoppingList.orderedProducts.forEach { listProduct ->
                out.beginObject()
                out.name("quantity")
                out.value(listProduct.quantity)
                out.name("product")
                ProductTypeAdapter.write(out, listProduct.product)
                out.endObject()
            }
            out.endArray()
            out.endObject()
        }

    }

    override fun read(`in`: JsonReader?): ShoppingList {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}