package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
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

class Manager_NotificationActivity : AppCompatActivity() {
    private lateinit var firestoreUserModel: FirestoreUserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_notification)  // manager_notification 레이아웃 로드
        setupNavigationButtons()  // 네비게이션 버튼 설정

        // Firestore 모델 인스턴스 생성 및 알람 가져오기
        firestoreUserModel = FirestoreUserModel()
        firestoreUserModel.getAlarms { status, messages ->
            if (status == StatusCode.SUCCESS) {
                displayMessages(messages)  // 알림 메시지 표시
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
        val managerMenuButton: ImageButton = findViewById(R.id.manager_menuButton)
        managerMenuButton.setOnClickListener {
            showPopupMenu(it)  // 팝업 메뉴 보여주기
        }

        val managerListButton: ImageButton = findViewById(R.id.manager_list)
        val managerRentalButton: ImageButton = findViewById(R.id.manager_rental)
        val managerCameraButton: ImageButton = findViewById(R.id.manager_camera)
        val managerMypageButton: ImageButton = findViewById(R.id.manager_mypage)

        // 각 버튼 클릭 시 해당 액티비티로 이동
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
    }

    // 팝업 메뉴 표시
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.managermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    // 메뉴 아이템 클릭 처리
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