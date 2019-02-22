package com.sdp15.goodb0i.view.saved_lists

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.lists.ListItem
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_order.view.*
import kotlin.math.max


class ListAdapter(private val orders: ArrayList<String>,private val showFullList: Boolean,private val dragListener: (RecyclerView.ViewHolder) -> Unit = {}): RecyclerView.Adapter<ListAdapter.ItemViewHolder>() {

    private var lists: MutableList<ShoppingList> = mutableListOf()
    private var expanded: MutableList<Boolean> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_order, parent, false))
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    override fun onBindViewHolder(holder: ListAdapter.ItemViewHolder, position: Int) {
        holder.view.apply {
//            item_button_container.visibility = if (expanded[holder.adapterPosition]) View.VISIBLE else View.GONE
            image_item_drag_handle.visibility = if (showFullList) View.VISIBLE else View.GONE
            //TODO: Why is this necessary
            // As soon as any style is set on the Textviews, touch events cause them to switch to a black text color
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
//                    if (expanded[holder.adapterPosition]) {
//                        item_button_container.visibility = View.GONE
//                    } else {
//                        item_button_container.visibility = View.VISIBLE
//                    }
                    expanded[holder.adapterPosition] = !expanded[holder.adapterPosition]
                }
                true
            }
            image_item_drag_handle.setOnTouchListener { view, motionEvent ->
                if (MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_DOWN) {
                    dragListener(holder)
                }
                true
            }
            val orderID = lists[holder.adapterPosition].code
            var time = lists[holder.adapterPosition].time
            text_order_date.text = orderID
        }
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}
