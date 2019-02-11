package service

import model.*
import org.jetbrains.exposed.sql.transactions.transaction

class ShelfService {


    fun initDefaultShelves() {
        createDefaultRacks()
    }

    private fun createDefaultRacks() {
        // Dairy, bakery, fruits, vegetables, seafood, meat, sweets, food cupboard
        transaction {
            val racks: MutableList<ShelfRack> = mutableListOf()
            
            
            
            arrayOf("Dairy", "Bakery", "Fruits", "Vegetables", "Seafood", "Meat", "Sweets", "Food cupboard").forEach {
                println("Inserting rack $it")
                racks.add(ShelfRack.new {
                    info = it
                    capacity = 10
                })
            }
            var count = 0
            Stock.all().forEach { stock ->
                when {
                    arrayOf("Milk", "Cheese").any { stock.department.contains(it) } -> {
                        println("Inserting dairy shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Dairy" }.id
                        }

                    }
                    stock.department.contains("Meat") -> {
                        println("Inserting meat shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Meat" }.id
                        }
                    }
                    arrayOf("Vegetables", "Salad").any { stock.department.contains(it) } -> {
                        println("Inserting veg shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Vegetables" }.id
                        }
                    }
                    stock.department.contains("Fruit") -> {
                        println("Inserting fruits shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Fruits" }.id
                        }
                    }
                    arrayOf("Bread", "Dough").any { stock.department.contains(it)} -> {

                        println("Inserting bakery shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Bakery" }.id
                        }
                    }
                    stock.department.contains("Fish") -> {
                        println("Inserting seafood shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Seafood" }.id
                        }
                    }
                    arrayOf("Cereal", "Table", "Pasta").any { stock.department.contains(it)} -> {
                        println("Inserting cupoard shelf for ${stock.department}")
                        Shelf.new {
                            position = count % 3
                            quantity = 5
                            product = stock.id
                            rack = racks.first { it.info == "Food cupboard" }.id
                        }
                    }
                    arrayOf("Choc", "Sweet", "Crisp").any { stock.department.contains(it) } -> {
                        println("Inserting sweets shelf for ${stock.department}")
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

}