package fr.em.orgaecls.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.R

class WidgetAdapter(
    itemList: MutableList<String>,
    val clickable: Boolean
) : RecyclerView.Adapter<WidgetAdapter.ViewHolder>() {
    val itemList = itemList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val widgetName = view.findViewById<TextView>(R.id.widget_name_textView)
        val removeButton = view.findViewById<ImageView>(R.id.widget_remove_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_widget, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentWidget = itemList[position]

        holder.widgetName.text = currentWidget
        if (clickable) {
            holder.removeButton.setOnClickListener {
                val position = holder.adapterPosition
                if (position >= 0 && position < itemList.size) deleteItem(holder.adapterPosition)
            }
        } else {
            holder.removeButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = itemList.size

    private fun deleteItem(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun insertItem(item: String) {
        if (!itemList.contains(item)) {
            itemList.add(item)
            notifyItemInserted(itemList.size - 1)
        }
    }
}