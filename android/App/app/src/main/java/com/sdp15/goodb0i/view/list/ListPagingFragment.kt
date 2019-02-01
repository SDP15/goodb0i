package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sdp15.goodb0i.R
import kotlinx.android.synthetic.main.layout_list_creation.*

class ListPagingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_list_creation, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val vp = ViewPagerAdapter(childFragmentManager)
        list_viewpager.adapter = vp
        //list_tab_layout.setupWithViewPager(list_viewpager)
    }

    private class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        private val searchFragment = SearchFragment()
        private val listFragment = ShoppingListFragment()

        override fun getItem(position: Int): Fragment {
            return if (position == 0) searchFragment else listFragment
        }

        override fun getCount(): Int = 2

        override fun getPageTitle(position: Int): CharSequence? {

            return if (position == 0) "Search" else "Trolley"
        }
    }

}