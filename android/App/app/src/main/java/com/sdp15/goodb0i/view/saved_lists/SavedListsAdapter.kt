package com.sdp15.goodb0i.view.saved_lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.android.synthetic.main.list_order.view.*

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
        holder.itemView.text_list_code.text = lists[position].code.toString()
        holder.itemView.text_list_price.text = holder.itemView.context.getString(R.string.label_total_price,
            lists[position].products.sumByDouble { it.quantity * it.product.price })
        holder.itemView.setOnClickListener {
            onClick(lists[position])
        }
    }

    class SavedListViewHolder(view: View) : RecyclerView.ViewHolder(view)

}