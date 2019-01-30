package service

import model.Shelfs
import model.Shelf
import org.jetbrains.exposed.sql.selectAll
import service.DatabaseFactory.dbQuery

class ShelfService {

    suspend fun getAllShelves(): List<Shelf> = dbQuery{
        Shelfs.selectAll().map()
    }

}