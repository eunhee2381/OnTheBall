package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreUserModel

class Manager_BlacklistActivity : AppCompatActivity() {
    private lateinit var blacklistRecyclerView: RecyclerView
    private lateinit var blacklistAdapter: BlacklistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_blacklist)
        setupNavigationButtons()
        setupRecyclerView()


        fetchBlacklistData()
    }
    private fun setupRecyclerView() {
        blacklistRecyclerView = findViewById(R.id.blacklist_recycler_view)
        blacklistRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchBlacklistData() {
        val firestoreUserModel = FirestoreUserModel()
        firestoreUserModel.getBlacklist { status, users ->
            if (status == StatusCode.SUCCESS && users != null) {
                blacklistAdapter = BlacklistAdapter(users)
                blacklistRecyclerView.adapter = blacklistAdapter
            } else {
                // Handle the error
            }
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // 버튼들 인식
        val back_Button: ImageButton = findViewById(R.id.blacklist_back_button)
        val managerListButton: ImageButton = findViewById(R.id.manager_list)
        val managerRentalButton: ImageButton = findViewById(R.id.manager_rental)
        val managerCameraButton: ImageButton = findViewById(R.id.manager_camera)
        val managerMypageButton: ImageButton = findViewById(R.id.manager_mypage)
        val blacklistModifyButton: ImageButton = findViewById(R.id.go_blacklist_modify)
        val managerAlarmButton: ImageButton = findViewById(R.id.manager_alarm)


        managerAlarmButton.setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }

        back_Button.setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }
        managerListButton.setOnClickListener {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        }
        managerRentalButton.setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }
        managerCameraButton.setOnClickListener {
            startActivity(Intent(this, Manager_CameraActivity::class.java))
        }
        managerMypageButton.setOnClickListener {
            startActivity(Intent(this, Manager_MypageActivity::class.java))
        }
        blacklistModifyButton.setOnClickListener {
            startActivity(Intent(this, Manager_Blacklist_ModifyActivity::class.java))
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
                startActivity(Intent(this, Manager_CameraActivity::class.java))
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
