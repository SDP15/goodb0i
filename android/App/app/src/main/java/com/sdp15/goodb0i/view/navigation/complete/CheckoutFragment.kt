package com.sdp15.goodb0i.view.navigation.complete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_checkout.*
import kotlinx.android.synthetic.main.layout_list_creation.*
import org.koin.android.ext.android.inject

class CheckoutFragment : BaseFragment() {

    private val vm: CheckoutViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_checkout, container, false)
    }

    override fun onStart() {
        super.onStart()
        checkout_viewpager.adapter = ViewPagerAdapter(childFragmentManager)
    }

    override fun onResume() {
        super.onResume()
        vm.bind()
    }

    override fun onBackPressed(): Boolean {
        if (list_viewpager.currentItem == 1) {
            list_viewpager.setCurrentItem(0, true)
            return true
        }
        return false
    }

    private class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val totalFragment = CheckoutTotalFragment()
        private val listFragment = CheckoutListFragment()

        override fun getItem(position: Int): Fragment {
            return if (position == 0) totalFragment else listFragment
        }

        override fun getCount(): Int = 2
    }
}