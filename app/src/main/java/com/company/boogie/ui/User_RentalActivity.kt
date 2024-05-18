package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class User_RentalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_rental)
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.user_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }


        val userListButton: ImageButton = findViewById(R.id.user_list)
        val userRentalButton: ImageButton = findViewById(R.id.user_rental)
        val userAlarmButton: ImageButton = findViewById(R.id.user_alarm)
        val userMypageButton: ImageButton = findViewById(R.id.user_mypage)

        userListButton.setOnClickListener {
            startActivity(Intent(this, User_ListActivity::class.java))
        }
        userRentalButton.setOnClickListener {
            startActivity(Intent(this, User_RentalActivity::class.java))
        }
        userAlarmButton.setOnClickListener {
            startActivity(Intent(this, User_NotificationActivity::class.java))
        }
        userMypageButton.setOnClickListener {
            startActivity(Intent(this, User_MypageActivity::class.java))
        }
    }

    private fun showPopupMenu(view: View) { // 팝업창
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.usermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.usermenulist -> {
                startActivity(Intent(this, User_ListActivity::class.java))
                true
            }
            R.id.usermenumypage -> {
                startActivity(Intent(this, User_MypageActivity::class.java))
                true
            }
            R.id.usermenurental -> {
                startActivity(Intent(this, User_RentalActivity::class.java))
                true
            }
            R.id.usermenunotification -> {
                startActivity(Intent(this, User_NotificationActivity::class.java))
                true
            }
            R.id.menudevelop -> {
                startActivity(Intent(this, DeveloperActivity::class.java)) // Ensure this activity is correctly implemented
                true
            }
            else -> false
        }
    }
}