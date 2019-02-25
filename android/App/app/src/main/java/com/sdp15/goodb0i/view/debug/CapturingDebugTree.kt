package com.sdp15.goodb0i.view.debug

import android.content.Context
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import timber.log.Timber

/**
 * Logging tree which saves the log for display in debug dialog
 */
object CapturingDebugTree : Timber.DebugTree() {

    private val builder = StringBuilder()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        builder.append("$tag : $message ${t ?: ""}\n")
    }

    fun showLog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Log")
            setMessage(builder.toString())
            setCancelable(false)
            setOnKeyListener { p0, p1, p2 ->
                if (p2.keyCode == KeyEvent.KEYCODE_BACK) {
                    p0?.dismiss()
                }
                false
            }
        }.show()
    }

}