package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)  // login 레이아웃을 불러옵니다.

        // 로그인 버튼 클릭 이벤트
        findViewById<Button>(R.id.signinbutton).setOnClickListener {
            // 로그인 처리 로직 (예: Firebase 로그인)
        }

        // 회원가입 버튼 클릭 이벤트
        findViewById<Button>(R.id.signupbutton).setOnClickListener {
            // JoinActivity로 이동
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }
}
