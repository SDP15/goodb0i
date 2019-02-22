package com.sdp15.goodb0i.view.list.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.BaseFragment
import com.sdp15.goodb0i.view.list.ListPagingFragmentArgs
import kotlinx.android.synthetic.main.layout_list_creation.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment hosting [SearchFragment] and [ShoppingListFragment] in a ViewPager
 */
class ListPagingFragment : BaseFragment() {

    val vm: ListViewModel by viewModel()
    private var shouldCloseOnBack = true

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

    override fun onStart() {
        super.onStart()
        val vp = ViewPagerAdapter(childFragmentManager)
        list_viewpager.adapter = vp
        //list_tab_layout.setupWithViewPager(list_viewpager)

    }

    override fun onResume() {
        super.onResume()
        val args = navArgs<ListPagingFragmentArgs>()
        val list = args.value.shoppingList
        if (list != null) {
            vm.setList(list)
            list_viewpager.currentItem = 1
            shouldCloseOnBack = true
        }
        floating_search_view.setOnMenuItemClickListener {
            list_viewpager.apply {
                setCurrentItem(if (currentItem == 1) 0 else 1, true)
            }
        }
        list_viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                shouldCloseOnBack = position == 0
                if (position == 1) {
                    floating_search_view.clearSearchFocus()
                }
                floating_search_view.inflateOverflowMenu(if (list_viewpager.currentItem == 0) R.menu.menu_search else R.menu.menu_shopping)
            }
        })
        vm.transitions.observe(this, Observer {
            if (baseActivity.fragmentHistory.first == R.id.list_confirmation_fragment && baseActivity.fragmentHistory[1] != R.id.list_creation_fragment) {
                findNavController().navigateUp()
            } else {
                findNavController().navigate(it)
            }
        })
    }

    override fun onBackPressed(): Boolean {
        if (!shouldCloseOnBack) {
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