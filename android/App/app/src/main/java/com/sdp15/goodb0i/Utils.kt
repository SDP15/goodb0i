package com.sdp15.goodb0i

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.view.animation.Animation
import android.view.animation.Transformation


fun EditText.watchText(action: (String) -> Unit) {
    addTextChangedListener(object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            action(s?.toString() ?: "")
        }
    })
}
//https://stackoverflow.com/questions/4946295/android-expand-collapse-animation
fun expand(v: View) {
    v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = v.getMeasuredHeight()

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    v.layoutParams.height = 1
    v.visibility = View.VISIBLE
    val a = object : Animation() {
        protected override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            v.layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            v.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms
    a.duration = (targetHeight / v.getContext().getResources().getDisplayMetrics().density).toLong()
    v.startAnimation(a)
}


fun collapse(v: View) {
    val initialHeight = v.getMeasuredHeight()

    val a = object : Animation() {
        protected override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                v.visibility = View.GONE
            } else {
                v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                v.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms
    a.duration = (initialHeight / v.getContext().getResources().getDisplayMetrics().density).toLong()
    v.startAnimation(a)
}