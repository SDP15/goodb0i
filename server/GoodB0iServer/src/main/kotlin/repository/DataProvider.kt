package repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import repository.lists.ListEntry
import repository.lists.ShoppingList
import repository.products.Product
import repository.shelves.Shelf
import repository.shelves.ShelfRack
import service.routing.Graph
import service.routing.Graph.Companion.graph
import java.io.File
import java.util.*

object DataProvider {

    private val kLogger = KotlinLogging.logger {}

    fun loadFromFile(productsPath: String, racksPath: String, graphPath: String, listsPath: String): Graph<Int> {
//        val file = File(path).bufferedReader()
//        val gson = Gson()
//        val data = gson.fromJson(file, Array<Any>::class.java)
        loadProducts(productsPath)
        loadShelfRacks(racksPath)
        loadListsFromFile(listsPath)
        return loadGraphFromFile(graphPath)
    }

    private fun loadProducts(productPath: String) {
        val file = File(productPath).bufferedReader()
        val products = Gson().fromJson(file, Array<JSONProduct>::class.java)
        transaction {
            if (!Product.all().empty()) return@transaction
            products.forEachIndexed { index, product ->

                val id = if (product.id == null) UUID.randomUUID() else UUID.fromString(product.id)
                //UUID.nameUUIDFromBytes(index.toString().toByteArray())
                kLogger.debug("Inserting product with ID $id")
                Product.new(id) {
                    gtin = product.gtin
                    name = product.name
                    averageSellingUnitWeight = product.averageSellingUnitWeight
                    contentsMeasureType = product.contentsMeasureType
                    contentsQuantity = product.contentsQuantity
                    unitOfSale = product.unitOfSale
                    unitQuantity = product.unitQuantity
                    department = product.department
                    description = product.description.joinToString("//")
                    price = product.price
                    superDepartment = product.superDepartment
                    unitPrice = product.unitPrice
                }
            }
            kLogger.info("Inserted ${products.size} products")

        }
    }

    private fun loadShelfRacks(rackPath: String) {
        val file = File(rackPath).bufferedReader()
        val racks = Gson().fromJson(file, Array<JSONShelfRack>::class.java)
        racks.forEach { rackJSON ->
            // Exposed doesn't seem to like inserting multiple racks in a single transaction
            transaction {

                val rackEntity = ShelfRack.new(rackJSON.id) {
                    info = rackJSON.info
                    capacity = rackJSON.capacity
                }
                kLogger.debug("Created new rack ${rackEntity.id} ${rackEntity.info}")
                rackJSON.shelves.forEach { shelfJSON ->
                    val productEntity = Product.findById(UUID.fromString(shelfJSON.product))
                    if (productEntity != null) {
                        Shelf.new {
                            position = shelfJSON.position
                            quantity = 5
                            product = productEntity
                            rack = rackEntity.id
                        }
                        kLogger.info("Created shelf for ${productEntity.name}")
                    } else {
                        kLogger.error("Could not find product with id ${shelfJSON.product} for shelf in rack $rackJSON")
                    }
                }
                kLogger.info("Created shelf rack ${rackJSON.id} (${rackJSON.info} with ${rackJSON.shelves.size} shelves)")
            }
            kLogger.debug("Finished shelf rack insertion")
        }
    }

    private fun loadListsFromFile(listsPath: String) {
        val file = File(listsPath).bufferedReader()
        val arr = Gson().fromJson(file, Array<JSONList>::class.java)
        arr.forEach { list ->
            transaction {
                if (!ShoppingList.all().empty()) return@transaction
                ShoppingList.new {
                    code = list.code
                    time = System.currentTimeMillis()
                    products = SizedCollection(
                            list.products.mapIndexed { i, listItemJSON ->
                                ListEntry.new {
                                    quantity = listItemJSON.quantity
                                    index = i
                                    product = Product[UUID.fromString((listItemJSON.product))]
                                }
                            }
                    )
                }
            }
        }
    }

    private fun loadGraphFromFile(graphPath: String): Graph<Int> {
        val file = File(graphPath).bufferedReader()
        val obj = JsonParser().parse(file).asJsonObject
        return graph {
            val defaultCost = 5
            obj.keySet().forEach { key ->
                when (key) {
                    "start" -> start(obj.getAsJsonPrimitive(key).asInt)
                    "end" -> end(obj.getAsJsonPrimitive(key).asInt)
                    else -> {
                        val out = obj.get(key)
                        when {
                            out.isJsonPrimitive -> key.toInt() center out.asInt cost defaultCost
                            out.isJsonObject -> out.asJsonObject.let { outs ->
                                if (outs.has("left")) {
                                    key.toInt() left outs.get("left").asInt cost defaultCost
                                }
                                if (outs.has("right")) {
                                    key.toInt() right outs.get("right").asInt cost defaultCost
                                }
                                if (outs.has("forward")) {
                                    key.toInt() center outs.get("forward").asInt cost defaultCost
                                }
                            }
                            else -> kLogger.error("Out value $out not valid")
                        }


                        kLogger.info("Adding edges from $key to $out")
                    }

                }
            }
            kLogger.debug("Load complete: nodes ${this.map { it.node.id }}")
        }
    }

    private data class JSONProduct(
            @SerializedName("id") val id: String? = null,
            @SerializedName("gtin") val gtin: String,
            @SerializedName("name") val name: String,
            @SerializedName("averageSellingUnitWeight") val averageSellingUnitWeight: Double,
            @SerializedName("contentsMeasureType") val contentsMeasureType: String,
            @SerializedName("contentsQuantity") val contentsQuantity: Double,
            @SerializedName("unitOfSale") val unitOfSale: Int,
            @SerializedName("unitQuantity") val unitQuantity: String,
            @SerializedName("department") val department: String,
            @SerializedName("description") val description: kotlin.collections.List<String>,
            @SerializedName("price") val price: Double,
            @SerializedName("superDepartment") val superDepartment: String,
            @SerializedName("unitPrice") val unitPrice: Double
    )

    private data class JSONShelfRack(
            @SerializedName("id") val id: Int,
            @SerializedName("capacity") val capacity: Int,
            @SerializedName("info") val info: String,
            @SerializedName("shelves") val shelves: List<JSONShelf>
    )

    private data class JSONShelf(
            @SerializedName("product") val product: String,
            @SerializedName("position") val position: Int
    )

    private data class JSONList(
            @SerializedName("code") val code: Long,
            @SerializedName("products") val products: List<JSONListItem>
    )

    private data class JSONListItem(
            @SerializedName("product") val product: String,
            @SerializedName("quantity") val quantity: Int
    )

}