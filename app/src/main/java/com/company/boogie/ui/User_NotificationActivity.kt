package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreUserModel
import com.company.boogie.models.Message


class User_NotificationActivity : AppCompatActivity() {
    private lateinit var firestoreUserModel: FirestoreUserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_notification)
        setupNavigationButtons()  // 설정 버튼 초기화

        // Firestore 모델 인스턴스 생성 및 알람 가져오기
        firestoreUserModel = FirestoreUserModel()
        firestoreUserModel.getAlarms { status, messages ->
            if (status == StatusCode.SUCCESS) {
                displayMessages(messages)  // 알람 메시지 표시
            }
        }
    }

    // 메시지를 동적으로 뷰에 추가하여 표시
    private fun displayMessages(messages: List<Message>?) {
        val container: LinearLayout = findViewById(R.id.messages_container)
        messages?.forEach { message ->
            val messageView = LayoutInflater.from(this).inflate(R.layout.message_item, container, false)
            messageView.findViewById<TextView>(R.id.message_title).text = message.title
            messageView.findViewById<TextView>(R.id.message_text).text = message.message
            container.addView(messageView)
        }
    }

    // 네비게이션 버튼 설정
    private fun setupNavigationButtons() {
        val managerMenuButton: ImageButton = findViewById(R.id.user_menuButton)
        managerMenuButton.setOnClickListener { showPopupMenu(it) }

        val userListButton: ImageButton = findViewById(R.id.user_list)
        val userRentalButton: ImageButton = findViewById(R.id.user_rental)
        val userAlarmButton: ImageButton = findViewById(R.id.user_alarm)
        val userMypageButton: ImageButton = findViewById(R.id.user_mypage)

        userListButton.setOnClickListener { startActivity(Intent(this, User_ListActivity::class.java)) }
        userRentalButton.setOnClickListener { startActivity(Intent(this, User_RentalActivity::class.java)) }
        userAlarmButton.setOnClickListener { startActivity(Intent(this, User_NotificationActivity::class.java)) }
        userMypageButton.setOnClickListener { startActivity(Intent(this, User_MypageActivity::class.java)) }
    }

    // 팝업 메뉴 표시
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.usermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.gravity = Gravity.END
        popup.show()
    }


    // 메뉴 아이템 클릭 처리
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
                startActivity(Intent(this, DeveloperActivity::class.java)) // 개발자 활동 확인
                true
            }
            else -> false
        }
    }
}
