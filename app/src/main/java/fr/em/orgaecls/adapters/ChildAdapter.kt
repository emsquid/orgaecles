package fr.em.orgaecls.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.models.ChildModel
import fr.em.orgaecls.popups.ChildPopup

class ChildAdapter(
    private val context: CampActivity,
    private val childList: List<ChildModel>
) : RecyclerView.Adapter<ChildAdapter.ViewHolder>() {

    private var childMutList = childList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val childCard = view.findViewById<CardView>(R.id.card_child)
        val childFirstname = view.findViewById<TextView>(R.id.card_child_firstname)
        val childName = view.findViewById<TextView>(R.id.card_child_name)
        val childAge = view.findViewById<TextView>(R.id.card_child_age)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_child, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentChild = childMutList[position]

        holder.childFirstname.text = currentChild.firstName
        holder.childName.text = currentChild.name
        holder.childAge.text = "${currentChild.age} ans"

        holder.childCard.setOnClickListener {
            ChildPopup(context, currentChild, currentChild.campUid).show()
        }
    }

    override fun getItemCount(): Int = childMutList.size

    private fun getItemPosition(childUid: String): Int {
        for (position in 0 until itemCount) {
            if (childMutList[position].uid == childUid) {
                return position
            }
        }
        return -1
    }

    fun insertItem(child: ChildModel) {
        childMutList.add(child)
        notifyItemInserted(itemCount - 1)
    }

    fun updateItem(child: ChildModel) {
        val position = getItemPosition(child.uid)
        if (position >= 0) {
            childMutList[position] = child
            notifyItemChanged(position)
        } else {
            insertItem(child)
        }
    }

    fun deleteItem(childUid: String) {
        val position = getItemPosition(childUid)
        if (position >= 0) {
            childMutList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}