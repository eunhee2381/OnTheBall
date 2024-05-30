package com.company.boogie.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserRentalAdapter(private val user: User) : RecyclerView.Adapter<UserRentalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.item_name)
        val borrowDate: TextView = view.findViewById(R.id.borrow_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_rental_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemName.text = user.borrowing
        Log.d("UserRentalAdapter", "Setting itemName: ${user.borrowing}")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val borrowDate = dateFormat.format(user.borrowAt)
        val calendar = Calendar.getInstance()
        calendar.time = user.borrowAt
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val returnDate = dateFormat.format(calendar.time)
        holder.borrowDate.text = "$borrowDate ~ $returnDate"
        Log.d("UserRentalAdapter", "Setting borrowDate: $borrowDate ~ $returnDate")
    }

    override fun getItemCount() = 1
}
