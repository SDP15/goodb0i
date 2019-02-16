package repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import repository.lists.ListContentsTable
import repository.lists.ShoppingLists
import repository.shelves.ShelfRacks
import repository.shelves.Shelves
import repository.products.Products

object DatabaseFactory {

    fun init() {
        // Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        Database.connect(hikari())
        transaction {
            create(Products)
            create(Shelves)
            create(ShelfRacks)
            create(ShoppingLists)
            create(ListContentsTable)
        }
    }


    /*
    Hikari is a JDBC connection pool
    It is used to keep connections open for re-use
    See https://stackoverflow.com/questions/4041114/what-is-database-pooling

     */
    private fun hikari(): HikariDataSource {
        // https://github.com/brettwooldridge/HikariCP
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            // Direct HikariCP to use "DriverManager-based" configuration
            jdbcUrl = "jdbc:h2:mem:test" //H2 in memory DB
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.validate()
        return HikariDataSource(config)
    }

}