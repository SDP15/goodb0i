package com.sdp15.goodb0i.data.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sdp15.goodb0i.data.store.cache.ListDAO
import com.sdp15.goodb0i.data.store.cache.ListItemTypeConverter
import com.sdp15.goodb0i.data.store.lists.ShoppingList

@Database(entities = [ShoppingList::class], version = 1)
@TypeConverters(ListItemTypeConverter::class)
abstract class RoomDB : RoomDatabase() {

    abstract fun listDAO(): ListDAO

}