package com.sdp15.goodb0i.view.debug

import android.content.Context
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.sdp15.goodb0i.data.store.RetrofitProvider

object Config {

    private val settings = mutableListOf<Pair<String, Boolean>>().apply {
        add(Pair("Test products data", false))

    }
    private val names: Array<String>
        get() = settings.map { it.first }.toTypedArray()
    private val setValues: BooleanArray
        get() = BooleanArray(settings.size) { i -> settings[i].second }

    val shouldUseTestData
        get() = settings[0].second

    fun showDialog(context: Context) {
        AlertDialog.Builder(context, android.R.style.ThemeOverlay_Material_Dark).apply {
            setTitle("Config")
            setMultiChoiceItems(
                names, setValues
            ) { di, pos, value -> settings[pos] = settings[pos].copy(second = value) }
            setNeutralButton(
                "Log"
            ) { p0, p1 -> CapturingDebugTree.showLog(context) }
            setNegativeButton("Root", {p0, p1 ->
                showRootConfigDialog(context)
            })
            setOnKeyListener { p0, p1, p2 ->
                if (p2.keyCode == KeyEvent.KEYCODE_BACK) {
                    p0?.dismiss()
                }
                false
            }
        }.show()
    }

    private fun showRootConfigDialog(context: Context) {
        val et = AppCompatEditText(context)
        et.setText(RetrofitProvider.root)
        AlertDialog.Builder(context).apply {
            setTitle("Root")
            setView(et)
            setCancelable(false)
            setOnKeyListener { p0, p1, p2 ->
                if (p2.keyCode == KeyEvent.KEYCODE_BACK) {
                    p0?.dismiss()
                }
                false
            }
            setPositiveButton("OK") { p0, p1 ->
                RetrofitProvider.root = et.text?.toString() ?: ""
            }
        }.show()
    }

}