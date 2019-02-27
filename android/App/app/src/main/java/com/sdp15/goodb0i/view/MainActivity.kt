package com.sdp15.goodb0i.view

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.CircularIntArray
import androidx.navigation.findNavController
import com.sdp15.goodb0i.data.navigation.sockets.SocketHandler
import com.sdp15.goodb0i.view.debug.Config
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import android.app.Activity
import androidx.core.content.ContextCompat.getSystemService
import android.R
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity() {

    //private val sh: SocketHandler by inject()

    val fragmentHistory = CircularIntArray(10)

    private var currentFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.sdp15.goodb0i.R.layout.activity_main)
        supportActionBar?.hide()
        findNavController(com.sdp15.goodb0i.R.id.nav_host_fragment).addOnDestinationChangedListener { controller, destination, arguments ->
            // Keep track of id of the previous fragment. Currently only used by ListConfirmationFragment
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(nav_host_fragment.view?.windowToken, 0)
            fragmentHistory.addFirst(currentFragment)
            currentFragment = destination.id
        }
        //sh.start("http://10.0.2.2:8080/ping", "first socket")
        //SocketHandler().start("http://10.0.2.2:8080/app", "second socket")

    }

    override fun onBackPressed() {
        val fragment = nav_host_fragment.childFragmentManager.fragments[0] // currently visible fragment
        // Pass the back press to the fragment for overriding
        val handled = (fragment as? BaseFragment)?.onBackPressed() ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Config.showDialog(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(com.sdp15.goodb0i.R.id.nav_host_fragment).navigateUp()



}
