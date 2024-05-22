package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.User
import com.company.boogie.utils.FirebaseUserUtil
import com.google.firebase.auth.FirebaseUser

class User_MypageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_mypage)
        setupNavigationButtons()

        val currentUser: FirebaseUser? = FirebaseUserUtil.whoAmI() // 로그인 정보

        // 계정 정보 보여주기
        if (currentUser == null) {
            Log.w("Manager_MypageActivity", "로그인 정보가 없음")
        }
        else {
            // 계정 정보 가져오기 성공 - UI 업데이트
            getUser(currentUser.uid) { STATUS_CODE, user ->
                if (STATUS_CODE == StatusCode.SUCCESS) {
                    findViewById<TextView>(R.id.adminname).text = user?.name ?: "No Name"
                    findViewById<TextView>(R.id.adminemail).text = user?.email ?: "No Email"
                }
            }
        }

        // 로그아웃 버튼 클릭 이벤트
        findViewById<Button>(R.id.logout_button).setOnClickListener {
            // 로그아웃 처리 로직
            doSignout()
        }

        // 탈퇴하기 버튼 클릭 이벤트
        findViewById<Button>(R.id.withdraw_button).setOnClickListener {
            // 탈퇴 처리 로직
            if (currentUser == null) {
                Log.w("Manager_MypageActivity", "로그인 정보가 없음")
            }
            else {
                leaveUser(currentUser.uid)
            }
        }
    }

    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.user_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)
        }

        // 버튼들 인식
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

    // 계정 정보 보여주기
    private fun getUser(uid: String, callback: (Int, User?) -> Unit) {
        // uid로 DB에서 사용자 정보 가져옴
        FirebaseUserUtil.getUser(uid) { STATUS_CODE, user ->
            if (STATUS_CODE == StatusCode.SUCCESS && user != null) {
                Log.d("Manager_MypageActivity", "[${uid}]:사용자명[${user.name}]:이메일[${user.email}] 사용자 정보 성공적으로 가져옴")
                callback(StatusCode.SUCCESS, user)
            }
            else {
                callback(StatusCode.FAILURE, null)
            }
        }
    }

    // 로그아웃
    private fun doSignout() {
        FirebaseUserUtil.doSignOut { STATUS_CODE ->
            // 로그아웃 성공 - LoginActivity 실행
            if (STATUS_CODE == StatusCode.SUCCESS) {
                Toast.makeText(this, "로그아웃에 성공했습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            // 로그아웃 실패 - Toast 메시지 띄우기
            else {
                Toast.makeText(this, "로그아웃에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 탈퇴하기
    private fun leaveUser(uid: String) {
        FirebaseUserUtil.leaveUser(uid) { STATUS_CODE ->
            if (STATUS_CODE == StatusCode.SUCCESS) {
                Toast.makeText(this, "계정 탈퇴에 성공했습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            else {
                Toast.makeText(this, "계정 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}