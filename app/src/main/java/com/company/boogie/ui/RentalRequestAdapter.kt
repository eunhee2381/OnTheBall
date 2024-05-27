package com.company.boogie.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RentalRequestAdapter : RecyclerView.Adapter<RentalRequestAdapter.RentalRequestViewHolder>() {

    private var rentalRequests: List<User> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rental_request, parent, false)
        return RentalRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalRequestViewHolder, position: Int) {
        val user = rentalRequests[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return rentalRequests.size
    }

    fun submitList(list: List<User>) {
        rentalRequests = list
        notifyDataSetChanged()
    }

    class RentalRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameBorrowingTextView: TextView = itemView.findViewById(R.id.nameBorrowingTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(user: User) {
            nameBorrowingTextView.text = "${user.name} : ${user.borrowing}"

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 현재 날짜와 일주일 후의 날짜를 계산
            val calendar = Calendar.getInstance().apply {
                time = user.borrowAt
                add(Calendar.DATE, 7)
            }
            val endDate = calendar.time

            val formattedStartDate = dateFormat.format(user.borrowAt)
            val formattedEndDate = dateFormat.format(endDate)
            dateTextView.text = "$formattedStartDate~$formattedEndDate"
        }
    }
}
