package com.sdp15.goodb0i.view.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.LayoutInflaterCompat
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.collapse
import com.sdp15.goodb0i.data.store.Item
import com.sdp15.goodb0i.expand
import kotlinx.android.synthetic.main.list_item.view.*
import timber.log.Timber

class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    var items: List<Item> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.item = items[position]
    }

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val buttons = view.findViewById<LinearLayout>(R.id.item_button_container)
        private val name = view.findViewById<AppCompatTextView>(R.id.text_item_name)
        private val price = view.findViewById<AppCompatTextView>(R.id.text_item_price)
        private val quantity = view.findViewById<AppCompatTextView>(R.id.text_item_quantity)
        var item: Item? = null
            set(value) {
                field = value
                value?.let {
                    name.text = it.name
                    price.text = it.price.toString()
                    quantity.text = it.contentsQuantity.toString()
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