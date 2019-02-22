package com.sdp15.goodb0i

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber
import java.util.ArrayList

object AppPreferences {
    private const val NAME = "goodboi.prefs"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    // list of app specific preferences
    private val IS_FIRST_RUN_PREF = Pair("is_first_run", false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    fun addOrder(code: String){
        val editor = preferences!!.edit()
        val value= preferences.getString("ordersIds",null)+","+code
        editor.putString("ordersIds",value)
        editor.apply()
        Timber.v("Prefereces Added")
    }

    fun getOrders():List<String>{

        val orders = preferences.getString("ordersIds", null) ?: return emptyList()
        return orders.split(",")

    }


}