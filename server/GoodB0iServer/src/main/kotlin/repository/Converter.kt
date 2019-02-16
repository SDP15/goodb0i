package repository

import com.google.gson.GsonBuilder
import repository.adapters.ListTypeAdapter
import repository.adapters.ProductTypeAdapter
import repository.lists.ShoppingList
import repository.products.Product

fun exposedTypeAdapters(): GsonBuilder.() -> Unit = {
    setPrettyPrinting()
    registerTypeAdapter(Product::class.java, ProductTypeAdapter)
    registerTypeAdapter(ShoppingList::class.java, ListTypeAdapter)
}