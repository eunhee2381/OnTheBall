package com.company.boogie.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.models.User

class BlacklistModifyAdapter(
    private val users: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<BlacklistModifyAdapter.BlacklistViewHolder>() {

    class BlacklistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.blacklist_username)
        val borrowingTextView: TextView = view.findViewById(R.id.blacklist_item)
        val deleteButton: Button = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlacklistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.blacklist_delete_item, parent, false)
        return BlacklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlacklistViewHolder, position: Int) {
        val user = users[position]
        holder.userNameTextView.text = user.name
        holder.borrowingTextView.text = "미반납: ${user.borrowing}"
        holder.deleteButton.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount(): Int = users.size
}
