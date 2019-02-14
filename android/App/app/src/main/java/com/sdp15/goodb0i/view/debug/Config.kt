package com.sdp15.goodb0i.view.debug

import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.sdp15.goodb0i.R

object Config {

    private val settings = mutableListOf<Pair<String, Boolean>>().apply {
        add(Pair("Test item data", false))
    }
    private val names: Array<String>
        get() = settings.map { it.first }.toTypedArray()
    private val setValues: BooleanArray
        get() = BooleanArray(settings.size) { i -> settings[i].second}

    val shouldUseTestData
        get() = settings[0].second

    fun showDialog(context: Context) {
        AlertDialog.Builder(context, android.R.style.ThemeOverlay_Material_Dark).apply {
            setTitle("Config")
            setMultiChoiceItems(names, setValues
            ) { di, pos, value -> settings[pos] = settings[pos].copy(second=value) }
            setNeutralButton("Log"
            ) { p0, p1 -> CapturingDebugTree.showLog(context) }
            setOnKeyListener { p0, p1, p2 ->
                if (p2.keyCode == KeyEvent.KEYCODE_BACK) {
                    p0?.dismiss()
                }
                false
            }
        }.show()
    }

}