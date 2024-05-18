package com.company.boogie.ui

import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class Manager_Delete_DialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        setContentView(R.layout.manager_delete_dialog)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val btnDelete: Button = findViewById(R.id.btn_delete)
        val btnCancel: Button = findViewById(R.id.btn_cancel)

        btnDelete.setOnClickListener {
            // 삭제 동작 추가
            finish() // 다이얼로그 닫기
        }

        btnCancel.setOnClickListener {
            finish() // 다이얼로그 닫기
        }
    }
}
