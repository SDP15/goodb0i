package com.sdp15.goodb0i.view.list

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.Item
import kotlinx.android.synthetic.main.list_item.view.*
import kotlin.math.max

class ItemAdapter(val onIncrement: (Item) -> Unit, val onDecrement: (Item) -> Unit,val total:Boolean) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var items: MutableList<TrolleyItem> = mutableListOf()
    private var expanded: MutableList<Boolean> = mutableListOf()
    private var keeptotal=total
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
            //TODO: Why is this necessary
            // As soon as any style is set on the Textviews, touch events cause them to switch to a black text color
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) view.callOnClick()
                true
            }
            setOnClickListener {
                if (expanded[position]) {
                    item_button_container.visibility = View.GONE
                    //collapse(item_button_container)
                } else {
                    item_button_container.visibility = View.VISIBLE
                    //expand(item_button_container)
                }
                expanded[position] = !expanded[position]
            }
            val item = items[position].item
            var quantity = items[position].count
            text_item_name.text = item.name
            if(item.description.first()!=null) {
                text_item_descr.text = item.description.first()
            }
            var tprice=getPrice(position)
            text_item_price.text = context.getString(R.string.label_item_price, tprice)
            text_item_quantity.text = quantity.toString()
            button_positive.setOnClickListener {
                onIncrement(item)
                items[position].count = ++quantity
                text_item_quantity.text = quantity.toString()
                tprice=getPrice(position)
                text_item_price.text = context.getString(R.string.label_item_price, tprice)
            }
            button_negative.setOnClickListener {
                onDecrement(item)
                quantity = max(0, quantity - 1)
                items[position].count = quantity
                text_item_quantity.text = quantity.toString()
                tprice=getPrice(position)
                text_item_price.text = context.getString(R.string.label_item_price, tprice)
            }

        }
    }
    fun getPrice(position: Int):Double{
        val item = items[position].item
        var quantity = items[position].count
        var tprice: Double
        if (keeptotal){
            tprice=item.price*quantity
        }else{
            tprice=item.price
        }
        return tprice
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}