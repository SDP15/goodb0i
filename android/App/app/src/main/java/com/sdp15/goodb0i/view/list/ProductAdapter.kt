package com.sdp15.goodb0i.view.list

import android.view.*
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.products.Product
import com.sdp15.goodb0i.move
import com.sdp15.goodb0i.view.ListDiff
import kotlinx.android.synthetic.main.list_item.view.*
import timber.log.Timber
import kotlin.math.exp
import kotlin.math.max

class ProductAdapter(val onIncrement: (Product) -> Unit, val onDecrement: (Product) -> Unit, private val showFullList: Boolean, private val dragListener: (RecyclerView.ViewHolder) -> Unit = {}) :
    RecyclerView.Adapter<ProductAdapter.ItemViewHolder>() {

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
                val i = diff.items.indexOf(diff.added)
                items = diff.items.toMutableList()
                expanded.add(i, false)
                notifyItemInserted(i)
            }
            is ListDiff.Remove -> {
                val i = items.indexOf(diff.removed)
                items = diff.items.toMutableList()
                expanded.removeAt(i)
                notifyItemRemoved(i)
            }
            is ListDiff.Update -> {
                val index = items.indexOfFirst { it.product == diff.updated.product }
                if (index != -1) {
                    items[index] = diff.updated
                    notifyItemChanged(index)
                }
            }
            is ListDiff.Move -> {
                items = diff.items.toMutableList()
                expanded.move(diff.from, diff.to)
                notifyItemMoved(diff.from, diff.to)
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.view.apply {
            item_button_container.visibility = if (expanded[holder.adapterPosition]) View.VISIBLE else View.GONE
            image_item_drag_handle.visibility = if (showFullList) View.VISIBLE else View.GONE
            //TODO: Why is this necessary
            // As soon as any style is set on the Textviews, touch events cause them to switch to a black text color
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    if (expanded[holder.adapterPosition]) {
                        item_button_container.visibility = View.GONE
                    } else {
                        item_button_container.visibility = View.VISIBLE
                    }
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
            val item = items[holder.adapterPosition].product
            var quantity = items[holder.adapterPosition].count
            text_item_name.text = item.name
            if (item.description.firstOrNull() != null) {
                text_item_descr.text = item.description.first()
            }
            var tprice = getPrice(holder.adapterPosition)
            text_item_price.text = context.getString(R.string.label_item_price, tprice)
            text_item_quantity.text = quantity.toString()
            button_positive.setOnClickListener {
                onIncrement(item)
                items[holder.adapterPosition].count = ++quantity
                text_item_quantity.text = quantity.toString()
                tprice = getPrice(holder.adapterPosition)
                text_item_price.text = context.getString(R.string.label_item_price, tprice)
            }
            button_negative.setOnClickListener {
                onDecrement(item)
                quantity = max(0, quantity - 1)
                items[holder.adapterPosition].count = quantity
                text_item_quantity.text = quantity.toString()
                tprice = getPrice(holder.adapterPosition)
                text_item_price.text = context.getString(R.string.label_item_price, tprice)
            }

        }
    }

    private fun getPrice(position: Int): Double {
        val item = items[position].product
        val quantity = items[position].count
        return if (showFullList) item.price * quantity else item.price
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)


}