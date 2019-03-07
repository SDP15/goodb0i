package repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import repository.adapters.ShoppingListTypeAdapter
import repository.lists.ListEntry
import repository.lists.ShoppingList
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import repository.products.Product
import repository.shelves.Shelves
import java.io.File
import java.util.*

object TestDataProvider {

    private val kLogger = KotlinLogging.logger { }

    fun insert() {
        GlobalScope.launch(Dispatchers.IO) {

            kLogger.debug("Inserting test products data")
            transaction {
                getTestData().forEachIndexed { index, item ->
                    Product.new(UUID.nameUUIDFromBytes(index.toString().toByteArray())) {
                        name = item.name
                        averageSellingUnitWeight = item.averageSellingUnitWeight
                        contentsMeasureType = item.contentsMeasureType
                        contentsQuantity = item.contentsQuantity
                        unitOfSale = item.unitOfSale
                        unitQuantity = item.unitQuantity
                        department = item.department
                        description = item.description.joinToString("//")
                        price = item.price
                        superDepartment = item.superDepartment
                        unitPrice = item.unitPrice
                    }
                }
                kLogger.debug("Product insert complete. ${Product.all().count()} inserted")
            }

            createDefaultShelves()
            createTestList()
        }
    }

    private fun createDefaultShelves() {
        // Dairy, bakery, fruits, vegetables, seafood, meat, sweets, food cupboard
        transaction {
            val rackNames = arrayOf("Dairy", "Bakery", "Fruits", "Vegetables", "Seafood", "Meat", "Sweets", "Food cupboard")
            val racks: MutableList<ShelfRack> = mutableListOf()
            val defaultCapacity = 10
            kLogger.debug("Inserting racks $rackNames with capacity $defaultCapacity")

            rackNames.forEach {
                racks.add(ShelfRack.new {
                    info = it
                    capacity = defaultCapacity
                })
            }
            var count = 0
            kLogger.debug("Inserting shelves onto racks for default products data")
            Product.all().forEach { stock ->
                when {
                    arrayOf("Milk", "Cheese").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting dairy shelf for ${stock.department} ${stock.name}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Dairy" }.id
                        }

                    }
                    stock.department.contains("Meat") -> {
                        kLogger.debug("Inserting meat shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Meat" }.id
                        }
                    }
                    arrayOf("Vegetables", "Salad").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting veg shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Vegetables" }.id
                        }
                    }
                    stock.department.contains("Fruit") -> {
                        kLogger.debug("Inserting fruits shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Fruits" }.id
                        }
                    }
                    arrayOf("Bread", "Dough").any { stock.department.contains(it) } -> {

                        kLogger.debug("Inserting bakery shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Bakery" }.id
                        }
                    }
                    stock.department.contains("Fish") -> {
                        kLogger.debug("Inserting seafood shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Seafood" }.id
                        }
                    }
                    arrayOf("Cereal", "Table", "Pasta").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting cupoard shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Food cupboard" }.id
                        }
                    }
                    arrayOf("Choc", "Sweet", "Crisp").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting sweets shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock
                            rack = racks.first { it.info == "Sweets" }.id
                        }
                    }
                }
                count++
            }
        }
    }

    private fun createTestList() {
        transaction {
            ShoppingList.new {
                code = 1234567
                time = System.currentTimeMillis()
                products = SizedCollection(Product.all().take(10).mapIndexed { i, p ->
                    ListEntry.new {
                        index = i
                        product = p
                        quantity = i + 1
                    }
                })
            }
            println("Rack ids ${ShelfRack.all().map { it.id.value }}")
            val available = listOf(1, 3, 5, 7).map { ShelfRack[it] }.map { rack ->
                Shelf.find { Shelves.rack eq rack.id }.first().product
            }
            // 3 Fruits
            // 1 Dairy
            // 5 Seafood
            // 7 sweets
            val fruits = Shelf.find { Shelves.rack eq 3}.first().product
            val dairy = Shelf.find { Shelves.rack eq 1}.first().product
            val seafood = Shelf.find { Shelves.rack eq 5}.first().product
            val sweets = Shelf.find { Shelves.rack eq 7}.first().product
            ShoppingList.new {
                code = 7654321
                time = System.currentTimeMillis()
                products = SizedCollection(listOf(fruits, sweets).mapIndexed { i, p ->
                    ListEntry.new {
                        index = i
                        product = p
                        quantity = i + 1
                    }
                })
            }
            ShoppingList.new {
                code = 7654322
                time = System.currentTimeMillis()
                products = SizedCollection(listOf(fruits, dairy).mapIndexed { i, p ->
                    ListEntry.new {
                        index = i
                        product = p
                        quantity = i + 1
                    }
                })
            }
        }
    }

    private fun getTestData(): Array<Item> {
        val path = System.getProperty("user.dir") + "/src/main/resources/items.json"
        kLogger.debug("Reading test products data from $path")
        val file = File(path).bufferedReader()
        val gson = Gson()
        return gson.fromJson(file, Array<Item>::class.java)
    }
}

data class Item(
        @SerializedName("id") val id: Long = -1,
        @SerializedName("name") val name: String,
        @SerializedName("AverageSellingUnitWeight") val averageSellingUnitWeight: Double,
        @SerializedName("ContentsMeasureType") val contentsMeasureType: String,
        @SerializedName("contentsQuantity") val contentsQuantity: Double,
        @SerializedName("UnitOfSale") val unitOfSale: Int,
        @SerializedName("UnitQuantity") val unitQuantity: String,
        @SerializedName("department") val department: String,
        @SerializedName("description") val description: kotlin.collections.List<String>,
        @SerializedName("price") val price: Double,
        @SerializedName("superDepartment") val superDepartment: String,
        @SerializedName("unitPrice") val unitPrice: Double


)