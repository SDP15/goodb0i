package com.sdp15.goodb0i

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.EditText
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.RecyclerView

// Simplify use of TextWatcher to simple lambda
fun EditText.watchText(action: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s?.toString() ?: "")
        }
    })
}

// Assumes that the ViewSwitcher contains a RecyclerView and some other view
// Switches to the other view when the RecyclerView is empty
fun ViewSwitcher.switchOnEmpty(adapter: RecyclerView.Adapter<*>) {
    adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            if ((adapter.itemCount == 0 && nextView !is RecyclerView) ||
                (adapter.itemCount > 0 && nextView is RecyclerView)) {
                showNext()
            }
        }
    })
}

//https://stackoverflow.com/questions/4946295/android-expand-collapse-animation
fun expand(v: View) {
    v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = v.measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            v.layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            v.requestLayout()
        }

        override fun willChangeBounds() = true
    }

    // 1dp/ms
    a.duration = (targetHeight / v.context.resources.displayMetrics.density).toLong()
    v.startAnimation(a)
}


fun collapse(v: View) {
    val initialHeight = v.measuredHeight

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                v.visibility = View.GONE
            } else {
                v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                v.requestLayout()
            }
        }

        override fun willChangeBounds() = true

    }

    // 1dp/ms
    a.duration = (initialHeight / v.context.resources.displayMetrics.density).toLong()
    v.startAnimation(a)
}