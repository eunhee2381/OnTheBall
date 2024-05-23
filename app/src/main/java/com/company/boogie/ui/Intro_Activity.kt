package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity) // xml, Kotlin 소스 연결

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent) // 인트로 실행 후 바로 MainActivity로 넘어감.
            finish()
        }, 1000) // 1초 후 인트로 실행
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
