package com.company.boogie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.models.User

class BlacklistAdapter(private val users: List<User>) : RecyclerView.Adapter<BlacklistAdapter.BlacklistViewHolder>() {

    class BlacklistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.blacklist_item_name)
        val borrowingTextView: TextView = view.findViewById(R.id.blacklist_item_borrowing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlacklistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.blacklist_item, parent, false)
        return BlacklistViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlacklistViewHolder, position: Int) {
        val user = users[position]
        holder.userNameTextView.text = user.name
        holder.borrowingTextView.text = "미반납: ${user.borrowing}"
    }

    override fun getItemCount(): Int = users.size
}
