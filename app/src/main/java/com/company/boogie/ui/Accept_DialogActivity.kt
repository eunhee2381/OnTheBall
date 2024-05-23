package com.company.boogie.ui

import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class Accept_DialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        setContentView(R.layout.accept)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAccept: Button = findViewById(R.id.btn_accept)
        val btnRefuse: Button = findViewById(R.id.btn_refuse)

        btnAccept.setOnClickListener {
            // 허락 동작 추가
            finish() // 다이얼로그 닫기
        }

        btnRefuse.setOnClickListener {
            // 거절 동작 추가
            finish() // 다이얼로그 닫기
        }
    }
}