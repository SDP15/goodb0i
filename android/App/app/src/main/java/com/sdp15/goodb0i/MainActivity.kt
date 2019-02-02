package com.sdp15.goodb0i

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    override fun onBackPressed() {
        val fragment = nav_host_fragment.childFragmentManager.fragments[0] // currently visible fragment
        // Pass the back press to the fragment for overriding
        val handled = (fragment as? BaseFragment)?.onBackPressed() ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.nav_host_fragment).navigateUp()
}
