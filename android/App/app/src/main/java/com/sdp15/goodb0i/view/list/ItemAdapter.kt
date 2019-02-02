package com.sdp15.goodb0i.view.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.collapse
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.expand
import kotlinx.android.synthetic.main.list_item.view.*

class ItemAdapter(val onIncrement: (Item) -> Unit, val onDecrement: (Item) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var items: MutableList<TrolleyItem> = mutableListOf()
    private var expanded: MutableList<Boolean> = mutableListOf()
    fun itemsChanged(diff: ListDiff<TrolleyItem>) {
        when (diff) {
            is ListDiff.All -> {
                items = diff.items.toMutableList()
                expanded = items.map { false }.toMutableList()
                notifyDataSetChanged()
            }
            is ListDiff.Add -> {
                val i = diff.items.indexOf(diff.item)
                items = diff.items.toMutableList()
                expanded.add(i, false)
                notifyItemInserted(i)
            }
            is ListDiff.Remove -> {
                val i = items.indexOf(diff.item)
                items = diff.items.toMutableList()
                expanded.removeAt(i)
                notifyItemRemoved(i)
            }
            is ListDiff.Update -> {
                items = diff.items.toMutableList()
                notifyItemChanged(items.indexOf(diff.item))
            }
        }

    }

    sealed class ListDiff<T>(val items: List<T>) {
        class All<T>(items: List<T>) : ListDiff<T>(items)
        class Add<T>(items: List<T>, val item: T) : ListDiff<T>(items)
        class Remove<T>(items: List<T>, val item: T) : ListDiff<T>(items)
        class Update<T>(items: List<T>, val item: T) : ListDiff<T>(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.view.apply {
            item_button_container.visibility = if (expanded[position]) View.VISIBLE else View.GONE
            setOnClickListener {
                if (expanded[position]) {
                    collapse(item_button_container)
                } else {
                    expand(item_button_container)
                }
                expanded[position] = !expanded[position]
            }
            var quantity = items[position].count
            val item = items[position].item
            text_item_name.text = item.name
            text_item_descr.text = item.description[0]
            text_item_price.text = "£"+item.price.toString()
            quantity_txt.text = quantity.toString()
            button_positive.setOnClickListener {
                onIncrement(item)
                quantity++
                quantity_txt.text = quantity.toString()
            }
            button_negative.setOnClickListener {
                onDecrement(item)
                if(quantity>0) {
                    quantity--
                    quantity_txt.text = quantity.toString()
                }
            }

        }
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}