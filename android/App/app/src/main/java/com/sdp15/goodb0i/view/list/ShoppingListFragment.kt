package com.sdp15.goodb0i.view.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.view.list.confirmation.ListConfirmationFragmentArgs
import kotlinx.android.synthetic.main.layout_shoppinglist.*

class ShoppingListFragment : Fragment() {

    private val vm: ListViewModel by lazy { (parentFragment as ListPagingFragment).vm }

    override fun onResume() {
        super.onResume()
        list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = ProductAdapter(vm::incrementItem, vm::decrementItem, true, touchHelper::startDrag)
        list_recycler.adapter = adapter
        touchHelper.attachToRecyclerView(list_recycler)
        vm.list.observe(this, Observer {
            // If empty, we want to load all of the items, rather than just the last diff
            if (adapter.itemCount == 0) {
                adapter.itemsChanged(it.toAll())
            } else {
                adapter.itemsChanged(it)
            }
        })
        vm.totalPrice.observe(this, Observer {
            label_total_price.text = getString(R.string.label_item_price, it)
        })
        vm.actions.observe(this, Observer { action ->
            if (action is ListViewModel.ListAction.ToastAction) {
                Toast.makeText(context, action.text, Toast.LENGTH_LONG).show()
            }
        })
        vm.transitions.observe(this, Observer {
            findNavController().navigate(it)
        })
        list_save_button.setOnClickListener {
            vm.onSaveList()
        }
    }

    private val touchHelper = object :
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                vm.moveProduct(viewHolder.adapterPosition, target.adapterPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_shoppinglist, container, false)
    }

}