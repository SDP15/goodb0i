package service

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.Shelf
import model.ShelfRack
import model.Stock
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object TestDataProvider {

    private val kLogger = KotlinLogging.logger { }

    fun insert() {
        GlobalScope.launch(Dispatchers.IO) {

            kLogger.debug("Inserting test stock data")
            transaction {
                getTestData().forEach {
                    Stock.new {
                        name = it.name
                        averageSellingUnitWeight = it.averageSellingUnitWeight
                        contentsMeasureType = it.contentsMeasureType
                        contentsQuantity = it.contentsQuantity
                        unitOfSale = it.unitOfSale
                        unitQuantity = it.unitQuantity
                        department = it.department
                        description = it.description.joinToString("//")
                        price = it.price
                        superDepartment = it.superDepartment
                        unitPrice = it.unitPrice
                    }
                }
                kLogger.debug("Stock insert complete. ${Stock.all().count()} inserted")
            }

            createDefaultShelves()
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
            kLogger.debug("Inserting shelves onto racks for default stock data")
            Stock.all().forEach { stock ->
                when {
                    arrayOf("Milk", "Cheese").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting dairy shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Dairy" }.id
                        }

                    }
                    stock.department.contains("Meat") -> {
                        kLogger.debug("Inserting meat shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Meat" }.id
                        }
                    }
                    arrayOf("Vegetables", "Salad").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting veg shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Vegetables" }.id
                        }
                    }
                    stock.department.contains("Fruit") -> {
                        kLogger.debug("Inserting fruits shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Fruits" }.id
                        }
                    }
                    arrayOf("Bread", "Dough").any { stock.department.contains(it)} -> {

                        kLogger.debug("Inserting bakery shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Bakery" }.id
                        }
                    }
                    stock.department.contains("Fish") -> {
                        kLogger.debug("Inserting seafood shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Seafood" }.id
                        }
                    }
                    arrayOf("Cereal", "Table", "Pasta").any { stock.department.contains(it)} -> {
                        kLogger.debug("Inserting cupoard shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Food cupboard" }.id
                        }
                    }
                    arrayOf("Choc", "Sweet", "Crisp").any { stock.department.contains(it) } -> {
                        kLogger.debug("Inserting sweets shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Sweets" }.id
                        }
                    }
                }
                count++
            }
        }
    }

    private fun getTestData(): Array<Item> {
        val path = System.getProperty("user.dir") + "/src/main/resources/items.json"
        kLogger.debug("Reading test stock data from $path")
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