package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.models.User
import com.company.boogie.ui.adapter.RentalRequestAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Manager_RentalActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var rentalRequestAdapter: RentalRequestAdapter
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_rental)
        setupNavigationButtons()

        recyclerView = findViewById(R.id.recyclerView_rentalRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)
        rentalRequestAdapter = RentalRequestAdapter()
        recyclerView.adapter = rentalRequestAdapter

        fetchRentalRequests()
    }

    private fun fetchRentalRequests() {
        db.collection("User")
            .whereEqualTo("isBanned", false)// 블랙리스트에 있다면 가져오지 않음
            .get()
            .addOnSuccessListener { documents ->
                val rentalRequests = documents.mapNotNull {
                    val user = it.toObject(User::class.java)
                    if (user.borrowing.isNotEmpty()) user else null//비어있지 않을경우 가져옴
                }
                rentalRequestAdapter.submitList(rentalRequests)
            }
            .addOnFailureListener { e ->
                Log.w("Manager_RentalActivity", "대여목록을 가져올때 오류가 발생했습니다!", e)
            }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        val managerListButton: ImageButton = findViewById(R.id.manager_list)
        val managerRentalButton: ImageButton = findViewById(R.id.manager_rental)
        val managerCameraButton: ImageButton = findViewById(R.id.manager_camera)
        val managerMypageButton: ImageButton = findViewById(R.id.manager_mypage)
        val managerAlarmButton: ImageButton = findViewById(R.id.manager_alarm)
        val blackButton: ImageButton = findViewById(R.id.black)

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
        blackButton.setOnClickListener {
            startActivity(Intent(this, Manager_BlacklistActivity::class.java))
        }
        managerAlarmButton.setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
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
