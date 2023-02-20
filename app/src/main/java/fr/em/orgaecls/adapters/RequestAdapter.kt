package fr.em.orgaecls.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.CampAccessRepository
import fr.em.orgaecls.models.CampModel

class RequestAdapter(
    private val context: MainActivity,
    private val requestList: ArrayList<CampModel>
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    val requestMutList = requestList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val requestName = view.findViewById<TextView>(R.id.card_request_name)
        val requestCreator = view.findViewById<TextView>(R.id.card_request_creator)
        val refuseButton = view.findViewById<ImageView>(R.id.card_request_refuse_button)
        val acceptButton = view.findViewById<ImageView>(R.id.card_request_accept_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_request, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentRequest = requestMutList[position]

        holder.requestName.text = currentRequest.name
        holder.requestCreator.text = currentRequest.creatorEmail

        holder.refuseButton.setOnClickListener {
            buildConfirmDialog(context) {
                CampAccessRepository().removeAccessToCampFromUser(
                    context.user.email!!,
                    currentRequest.uid
                )
            }
        }

        holder.acceptButton.setOnClickListener {
            buildConfirmDialog(context) {
                CampAccessRepository().addAccessToCampForUser(
                    context.user.email!!,
                    currentRequest.uid
                )
            }
        }
    }

    override fun getItemCount(): Int = requestMutList.size

    private fun getItemPosition(campUid: String): Int {
        for (position in 0 until itemCount) {
            if (requestMutList[position].uid == campUid) {
                return position
            }
        }
        return -1
    }

    fun insertItem(camp: CampModel) {
        requestMutList.add(camp)
        notifyItemInserted(itemCount - 1)
    }

    fun updateItem(camp: CampModel) {
        val position = getItemPosition(camp.uid)
        if (position >= 0) {
            requestMutList[position] = camp
            notifyItemChanged(position)
        } else {
            insertItem(camp)
        }
    }

    fun deleteItem(campUid: String) {
        val position = getItemPosition(campUid)
        if (position >= 0) {
            requestMutList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}