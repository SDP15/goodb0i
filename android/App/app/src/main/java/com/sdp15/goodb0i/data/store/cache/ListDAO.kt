package com.sdp15.goodb0i.data.store.cache

import androidx.room.*
import com.sdp15.goodb0i.data.store.lists.ShoppingList

@Dao
interface ListDAO {

    @Query("SELECT * FROM ShoppingList")
    suspend fun loadAll(): List<ShoppingList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList)

    @Delete
    suspend fun delete(list: ShoppingList)
}