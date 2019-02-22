package com.sdp15.goodb0i.data.store.lists.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sdp15.goodb0i.data.store.lists.ShoppingList

@Dao
interface ListDAO {

    @Query("SELECT * FROM ShoppingList")
    suspend fun loadAll(): List<ShoppingList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList)

}