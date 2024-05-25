package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class Manager_DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager_detail)

        val editButton: Button = findViewById(R.id.button_edit) // 수정하기 버튼을 누를 경우 수정페이지로
        editButton.setOnClickListener {
            val intent = Intent(this, Manager_ModifyActivity::class.java)
            startActivity(intent)
        }

        val closeButton: Button = findViewById(R.id.close_button) // 나가기 버튼을 누를경우 리스트로 돌아감
        closeButton.setOnClickListener{
            val intent = Intent(this, Manager_ListActivity::class.java)
            startActivity(intent)
        }
        val managerAlarmButton: ImageButton = findViewById(R.id.manager_alarm)
        managerAlarmButton.setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }


        setupNavigationButtons()
    }

    private fun setupNavigationButtons() { // 하단바 버튼들
        findViewById<ImageButton>(R.id.manager_list).setOnClickListener {
            startActivity(Intent(this, Manager_ListActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_rental).setOnClickListener {
            startActivity(Intent(this, Manager_RentalActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_camera).setOnClickListener {
            startActivity(Intent(this, Manager_CameraActivity::class.java))
            //startActivity(Intent(this, ::class.java))
        }

        findViewById<ImageButton>(R.id.manager_mypage).setOnClickListener {
            startActivity(Intent(this, Manager_MypageActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_alarm).setOnClickListener {
            startActivity(Intent(this, Manager_NotificationActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manager_menuButton).setOnClickListener { view ->
            showPopupMenu(view)
        }

    }


    private fun showPopupMenu(view: View) { // 팦 메뉴창
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.managermenu, popup.menu)
        popup.setOnMenuItemClickListener { item -> onMenuItemClick(item) }
        popup.show()
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
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
                //startActivity(Intent(this, ::class.java))
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
