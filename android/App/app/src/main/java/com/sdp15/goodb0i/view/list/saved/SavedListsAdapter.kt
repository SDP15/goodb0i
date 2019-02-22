package com.sdp15.goodb0i.view.list.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.android.synthetic.main.list_order.view.*
import kotlin.math.min

class SavedListsAdapter(val onClick: (ShoppingList) -> Unit) : RecyclerView.Adapter<SavedListsAdapter.SavedListViewHolder>() {

    private val lists = mutableListOf<ShoppingList>()

    fun setItems(items: Collection<ShoppingList>) {
        lists.clear()
        lists.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedListViewHolder =
        SavedListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_order, parent, false))

    override fun getItemCount(): Int = lists.size

    override fun onBindViewHolder(holder: SavedListViewHolder, position: Int) {
        val sl = lists[position]
        holder.itemView.apply {
            text_list_code.text = sl.code.toString()
            text_list_price.text = context.getString(R.string.label_total_price,
                sl.products.sumByDouble { it.quantity * it.product.price })
            text_list_preview.text = sl.products.take(min(3, sl.products.size)).joinToString(separator = "\n") { item ->
                context.getString(R.string.label_list_item_info, item.quantity, item.product.name)
            }
            setOnClickListener {
                onClick(sl)
            }
        }
    }

    class SavedListViewHolder(view: View) : RecyclerView.ViewHolder(view)

}