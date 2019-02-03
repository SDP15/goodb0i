package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import kotlinx.android.synthetic.main.layout_list_creation.*
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getScope
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment hosting [SearchFragment] and [ShoppingListFragment] in a ViewPager
 */
class ListPagingFragment : BaseFragment() {

    val vm: ListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_list_creation, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.bind()

    }

    override fun onResume() {
        super.onResume()
        // Have to use childFragmentManager for nested fragments
        val vp = ViewPagerAdapter(childFragmentManager)
        list_viewpager.adapter = vp
        //list_tab_layout.setupWithViewPager(list_viewpager)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onBackPressed(): Boolean {
        if (list_viewpager.currentItem == 1) { // Switch from list to search
            list_viewpager.setCurrentItem(0, true)
            return true
        }
        return false
    }

    private class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val searchFragment = SearchFragment()
        private val listFragment = ShoppingListFragment()

        override fun getItem(position: Int): Fragment {
            return if (position == 0) searchFragment else listFragment
        }

        override fun getCount(): Int = 2
    }

}