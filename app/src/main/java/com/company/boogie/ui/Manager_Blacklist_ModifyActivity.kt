package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class Manager_Blacklist_ModifyActivity : AppCompatActivity() {

    private lateinit var additionNameEditText: EditText
    private lateinit var notReturnedEditText: EditText
    private lateinit var blacklistContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_blacklist_modify)

        additionNameEditText = findViewById(R.id.addition_name)
        notReturnedEditText = findViewById(R.id.not_returned)
        blacklistContainer = findViewById(R.id.blacklist_container)

        findViewById<Button>(R.id.add_blacklist_button).setOnClickListener {
            addBlacklistItem()
        }

        setupNavigationButtons()
    }

    private fun addBlacklistItem() {
        val name = additionNameEditText.text.toString()
        val notReturned = notReturnedEditText.text.toString()

        if (name.isNotEmpty() && notReturned.isNotEmpty()) {
            val newItem = layoutInflater.inflate(R.layout.blacklist_item, blacklistContainer, false)

            val nameTextView: TextView = newItem.findViewById(R.id.blacklist_name)
            val notReturnedTextView: TextView = newItem.findViewById(R.id.blacklist_not_returned)

            nameTextView.text = name
            notReturnedTextView.text = "미반납: $notReturned"

            blacklistContainer.addView(newItem)

            additionNameEditText.text.clear()
            notReturnedEditText.text.clear()
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // 버튼들 인식
        val managerListButton: ImageButton = findViewById(R.id.manager_list)
        val managerRentalButton: ImageButton = findViewById(R.id.manager_rental)
        val managerCameraButton: ImageButton = findViewById(R.id.manager_camera)
        val managerMypageButton: ImageButton = findViewById(R.id.manager_mypage)
        val backButton: ImageButton = findViewById(R.id.back_button)
        val managerAlarmButton: ImageButton = findViewById(R.id.manager_alarm)
        managerAlarmButton.setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }

        managerListButton.setOnClickListener {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        }
        managerRentalButton.setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }
        managerCameraButton.setOnClickListener {
            // 추가하기
        }
        managerMypageButton.setOnClickListener {
            startActivity(Intent(this, Manager_MypageActivity::class.java))
        }
        backButton.setOnClickListener {
            startActivity(Intent(this, Manager_BlacklistActivity::class.java))
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.managermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
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
}
