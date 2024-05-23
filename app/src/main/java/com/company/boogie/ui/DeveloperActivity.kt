package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class DeveloperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer)

        // 메뉴 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            showPopupMenu(it)
        }

        // 홈 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.home).setOnClickListener {
            navigateToHome()
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        val menuRes = if (LoginActivity.isAdmin) R.menu.managermenu else R.menu.usermenu
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            if (LoginActivity.isAdmin) {
                handleAdminMenuItemClick(item)
            } else {
                handleUserMenuItemClick(item)
            }
        }
        popup.show()
    }

    private fun handleUserMenuItemClick(item: MenuItem): Boolean {
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
                startActivity(Intent(this, DeveloperActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun handleAdminMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.managermenulist -> {
                startActivity(Intent(this, Manager_ListActivity::class.java))
                true
            }
            R.id.managermenumypage -> {
                startActivity(Intent(this, Manager_MypageActivity::class.java))
                true
            }
            R.id.managermenucamera -> {
                // 추가하기
                true
            }
            R.id.managermenurental -> {
                startActivity(Intent(this, Manager_RentalActivity::class.java))
                true
            }
            R.id.managermenunotification -> {
                startActivity(Intent(this, Manager_NotificationActivity::class.java))
                true
            }
            R.id.menudevelop -> {
                startActivity(Intent(this, DeveloperActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun navigateToHome() {
        if (LoginActivity.isAdmin) {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        } else {
            startActivity(Intent(this, User_ListActivity::class.java))
        }
    }
}
