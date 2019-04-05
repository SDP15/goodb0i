package com.sdp15.goodb0i.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.CircularIntArray
import androidx.navigation.findNavController
import com.sdp15.goodb0i.view.debug.ConfigActivity
import kotlinx.android.synthetic.main.activity_main.*


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
            startActivity(Intent(this, ConfigActivity::class.java))
            //Config.showDialog(this)
            return true
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val fragment = nav_host_fragment.childFragmentManager.fragments[0]
            (fragment as? BaseFragment)?.onVolumeUpPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(com.sdp15.goodb0i.R.id.nav_host_fragment).navigateUp()



}
