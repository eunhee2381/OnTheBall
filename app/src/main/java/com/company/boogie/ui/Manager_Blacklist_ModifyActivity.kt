package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreUserModel
import com.company.boogie.models.User

class Manager_Blacklist_ModifyActivity : AppCompatActivity() {

    private lateinit var additionNameEditText: EditText
    private lateinit var notReturnedEditText: EditText
    private lateinit var blacklistRecyclerView: RecyclerView
    private val firestoreUserModel = FirestoreUserModel()
    private lateinit var blacklistModifyAdapter: BlacklistModifyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_blacklist_modify)

        additionNameEditText = findViewById(R.id.addition_name)
        notReturnedEditText = findViewById(R.id.not_returned)
        blacklistRecyclerView = findViewById(R.id.blacklist_recyclerview)

        findViewById<Button>(R.id.add_blacklist_button).setOnClickListener {
            addUserToBlacklist()
        }

        findViewById<Button>(R.id.modify_button).setOnClickListener {
            navigateToBlacklistActivity()
        }

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            navigateToBlacklistActivity()
        }

        setupNavigationButtons()
        setupRecyclerView()
        loadBlacklist()
    }

    private fun addUserToBlacklist() {
        val email = additionNameEditText.text.toString()
        val notReturned = notReturnedEditText.text.toString()

        if (email.isNotEmpty() && notReturned.isNotEmpty()) {
            firestoreUserModel.getUserByEmail(email) { user ->
                if (user != null) {
                    user.isBanned = true
                    user.borrowing = notReturned
                    Log.d("Manager_Blacklist_ModifyActivity", "블랙리스트에 추가: $email")
                    firestoreUserModel.updateUser(user) { status ->
                        if (status == StatusCode.SUCCESS) {
                            Log.d("Manager_Blacklist_ModifyActivity", "블랙리스트 업데이트 성공: $email")
                            loadBlacklist()
                        } else {
                            Log.e("Manager_Blacklist_ModifyActivity", "블랙리스트 업데이트 실패: $email")
                        }
                    }
                } else {
                    Log.e("Manager_Blacklist_ModifyActivity", "사용자를 찾을 수 없음: $email")
                    showUserNotFoundDialog()  // 사용자 없음 다이얼로그 표시
                }
            }
        } else {
            Log.e("Manager_Blacklist_ModifyActivity", "이메일이나 미반납한 기자재명이 비어 있음")
        }
    }

    private fun showUserNotFoundDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("사용자 없음")
        builder.setMessage("없는 사용자입니다.")
        builder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()  // 다이얼로그 닫기
        }
        builder.create().show()
    }

    private fun setupRecyclerView() {
        blacklistRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadBlacklist() {
        firestoreUserModel.getBlacklist { status, users ->
            if (status == StatusCode.SUCCESS && users != null) {
                blacklistModifyAdapter = BlacklistModifyAdapter(users) { user ->
                    removeUserFromBlacklist(user)
                }
                blacklistRecyclerView.adapter = blacklistModifyAdapter
            }
        }
    }

    private fun removeUserFromBlacklist(user: User) {
        user.isBanned = false
        Log.d("Manager_Blacklist_ModifyActivity", "블랙리스트에서 제거: ${user.email}")
        firestoreUserModel.updateUser(user) { status ->
            if (status == StatusCode.SUCCESS) {
                Log.d("Manager_Blacklist_ModifyActivity", "블랙리스트 업데이트 성공: ${user.email}")
                loadBlacklist()
            } else {
                Log.e("Manager_Blacklist_ModifyActivity", "블랙리스트 업데이트 실패: ${user.email}")
            }
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
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

    private fun navigateToBlacklistActivity() {
        val intent = Intent(this, Manager_BlacklistActivity::class.java)
        startActivity(intent)
    }
}
