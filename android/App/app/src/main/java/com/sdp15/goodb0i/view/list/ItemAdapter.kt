package com.sdp15.goodb0i.view.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.collapse
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.expand
import timber.log.Timber

class ItemAdapter(val onIncrement: (Item) -> Unit, val onDecrement: (Item) -> Unit) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var items: MutableList<CartItem> = mutableListOf()

    fun itemsChanged(diff: ListDiff<CartItem>) {
        when (diff) {
            is ListDiff.All -> {
                items = diff.items.toMutableList()
                notifyDataSetChanged()
            }
            is ListDiff.Add -> {
                val i = diff.items.indexOf(diff.item)
                items = diff.items.toMutableList()
                notifyItemInserted(i)
            }
            is ListDiff.Remove -> {
                val i = items.indexOf(diff.item)
                items = diff.items.toMutableList()
                notifyItemRemoved(i)
            }
            is ListDiff.Update -> {
                items = diff.items.toMutableList()
                notifyItemChanged(items.indexOf(diff.item))
            }
        }
    }

    sealed class ListDiff<T>(val items: List<T>) {
        class All<T>(items: List<T>): ListDiff<T>(items)
        class Add<T>(items: List<T>, val item: T) : ListDiff<T>(items)
        class Remove<T>(items: List<T>, val item: T): ListDiff<T>(items)
        class Update<T>(items: List<T>, val item: T): ListDiff<T>(items)
    }

    fun itemCountChanged(item: CartItem) {
        val i = items.indexOfFirst { it.item.id == item.item.id }
        if (i != -1) {
            items[i] = item
            notifyItemChanged(i)
        } else {
            Timber.e("Item $item should have been in adapter, but wasn't")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.item = items[position].item
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val buttons = view.findViewById<LinearLayout>(R.id.item_button_container)
        private val name = view.findViewById<AppCompatTextView>(R.id.text_item_name)
        private val price = view.findViewById<AppCompatTextView>(R.id.text_item_price)
        private val quantity = view.findViewById<AppCompatTextView>(R.id.text_item_quantity)
        private val positive = view.findViewById<AppCompatImageButton>(R.id.button_positive)
        private val negative = view.findViewById<AppCompatImageButton>(R.id.button_negative)
        
        var item: Item? = null
            set(value) {
                field = value
                value?.let { item ->
                    name.text = item.name
                    price.text = item.price.toString()
                    quantity.text = item.contentsQuantity.toString()
                    positive.setOnClickListener { onIncrement(item) }
                    negative.setOnClickListener { onDecrement(item) }
                }
            }

        init {
            view.setOnClickListener {
                if (buttons.visibility == View.GONE) {
                    expand(buttons)
                } else {
                    collapse(buttons)
                }
            }
        }

    }

}