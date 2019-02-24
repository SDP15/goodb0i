package com.sdp15.goodb0i.view.list.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdp15.goodb0i.R
import com.sdp15.goodb0i.data.store.cache.ListDAO
import com.sdp15.goodb0i.data.store.cache.ShoppingListStore
import com.sdp15.goodb0i.data.store.lists.ShoppingList
import kotlinx.android.synthetic.main.list_order.*
import kotlinx.android.synthetic.main.list_order.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import kotlin.math.min

class SavedListsAdapter(val onClick: (ShoppingList) -> Unit) : RecyclerView.Adapter<SavedListsAdapter.SavedListViewHolder>(), KoinComponent {

    private val lists = mutableListOf<ShoppingList>()
    private val listStore: ShoppingListStore by inject()
    fun setItems(items: Collection<ShoppingList>) {
        lists.clear()
        lists.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedListViewHolder =
        SavedListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_order, parent, false))

    override fun getItemCount(): Int = lists.size

    suspend fun deleteList(pos: Int){
        listStore.deleteList(lists[pos])
        lists.removeAt(pos)
        //need to update the screen
    }


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
            delete_order_btn.setOnClickListener(){
                GlobalScope.launch(Dispatchers.IO){
                    deleteList(position)
                   }

            }
        }


    }

    class SavedListViewHolder(view: View) : RecyclerView.ViewHolder(view)

}