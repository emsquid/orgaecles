package fr.em.orgaecls.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.models.CampModel
import fr.em.orgaecls.popups.CampPopup

class CampAdapter(
    private val context: MainActivity,
    private val campList: ArrayList<CampModel>
) : RecyclerView.Adapter<CampAdapter.ViewHolder>() {

    val campMutList = campList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val campItem = view.findViewById<CardView>(R.id.card_camp)
        val campName = view.findViewById<TextView>(R.id.card_camp_name)
        val campStart = view.findViewById<TextView>(R.id.card_camp_start)
        val campEnd = view.findViewById<TextView>(R.id.card_camp_end)
        val moreButton = view.findViewById<ImageView>(R.id.card_camp_more_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_camp, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCamp = campMutList[position]

        holder.campName.text = currentCamp.name
        holder.campStart.text = currentCamp.start
        holder.campEnd.text = currentCamp.end

        holder.moreButton.setOnClickListener {
            CampPopup(context, currentCamp).show()
        }

        holder.campItem.setOnClickListener {
            context.goToCampActivity(currentCamp)
        }
    }

    override fun getItemCount(): Int = campMutList.size

    private fun getItemPosition(campUid: String): Int {
        for (position in 0 until itemCount) {
            if (campMutList[position].uid == campUid) {
                return position
            }
        }
        return -1
    }

    fun insertItem(camp: CampModel) {
        campMutList.add(camp)
        notifyItemInserted(itemCount - 1)
    }

    fun updateItem(camp: CampModel) {
        val position = getItemPosition(camp.uid)
        if (position >= 0) {
            campMutList[position] = camp
            notifyItemChanged(position)
        } else {
            insertItem(camp)
        }
    }

    fun deleteItem(campUid: String) {
        val position = getItemPosition(campUid)
        if (position >= 0) {
            campMutList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}