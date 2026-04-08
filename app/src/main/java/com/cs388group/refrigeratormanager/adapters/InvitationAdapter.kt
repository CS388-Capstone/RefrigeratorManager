package com.cs388group.refrigeratormanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.data.InviteUI
import com.cs388group.refrigeratormanager.data.UserRepository

class InvitationAdapter(
    private var invites: List<InviteUI>,
    private val onAccept: (InviteUI) -> Unit,
    private val onDecline: (InviteUI) -> Unit
) : RecyclerView.Adapter<InvitationAdapter.InviteViewHolder>()
{

    class InviteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupText: TextView = view.findViewById(R.id.textGroup)
        val fromText: TextView = view.findViewById(R.id.textFrom)
        val acceptButton: Button = view.findViewById(R.id.buttonAccept)
        val declineButton: Button = view.findViewById(R.id.buttonDecline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invitation, parent, false)
        return InviteViewHolder(view)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val invite = invites[position]

        holder.groupText.text = "Group ${invite.groupName}"
        holder.fromText.text = "Owned By ${invite.fromUserName}"

        holder.acceptButton.setOnClickListener {
            onAccept(invite)
        }

        holder.declineButton.setOnClickListener {
            onDecline(invite)
        }

    }

    override fun getItemCount(): Int = invites.size

    fun updateData(newInvites: List<InviteUI>) {
        invites = newInvites
        notifyDataSetChanged()
    }


}