package com.sdp15.goodb0i.data.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import com.sdp15.goodb0i.data.store.lists.cache.ListDAO
import com.sdp15.goodb0i.data.store.lists.cache.ListItemTypeConverter

@Database(entities = [ShoppingList::class], version = 1)
@TypeConverters(ListItemTypeConverter::class)
abstract class RoomDB : RoomDatabase() {

    abstract fun listDAO(): ListDAO

}