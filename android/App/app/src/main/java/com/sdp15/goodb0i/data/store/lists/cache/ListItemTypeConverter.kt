package com.sdp15.goodb0i.data.store.lists.cache

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.sdp15.goodb0i.data.store.lists.ListItem

class ListItemTypeConverter{

    @TypeConverter
    fun listItemsToString(list: List<ListItem>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToListItems(string: String): List<ListItem> {
        return Gson().fromJson(string, Array<ListItem>::class.java).toList()
    }


}