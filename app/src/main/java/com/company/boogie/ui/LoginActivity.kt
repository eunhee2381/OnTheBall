package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.utils.FirebaseUserUtil

class LoginActivity : AppCompatActivity() {

    companion object { //로그인 사용자,관리자 정보 저장
        var isAdmin: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)  // login 레이아웃을 불러옵니다.

        // 로그인 버튼 클릭 이벤트
        findViewById<Button>(R.id.signinbutton).setOnClickListener {
            // UI 정보 가져오기
            val email = findViewById<EditText>(R.id.email)?.text.toString() // 이메일
            val password = findViewById<EditText>(R.id.password)?.text.toString() // 비밀번호

            // 로그인 처리 로직 (예: Firebase 로그인)
            doSignin(email, password)
        }

        // 회원가입 버튼 클릭 이벤트
        findViewById<Button>(R.id.signupbutton).setOnClickListener {
            // JoinActivity로 이동
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }

    // 로그인
    private fun doSignin(email: String, password: String) {
        FirebaseUserUtil.doSignIn(email, password) { STATUS_CODE, uid ->
            // 로그인 성공 - 관리자면 Manager_ListActivity, 사용자면 User_ListActivity 실행
            if (STATUS_CODE == StatusCode.SUCCESS && uid != null) {
                Toast.makeText(this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()

                // uid로 DB에서 사용자 정보 가져옴
                FirebaseUserUtil.getUser(uid) { STATUS_CODE, user ->
                    if (STATUS_CODE == StatusCode.SUCCESS && user != null) {
                        Log.d("LoginActivity", "[${uid}]:사용자명[${user.name}]:관리자여부[(${user.idAdmin})] 사용자 정보 성공적으로 가져옴")

                        // 관리자 계정이면 Manager_ListActivity 실행
                        if (user.idAdmin) {
                            isAdmin = true
                            Log.d("LoginActivity", "Manager_ListActivity 실행 (관리자 계정)")
                            startActivity(Intent(this, Manager_ListActivity::class.java))
                            finish()
                        }
                        // 사용자면 User_ListActivity 실행
                        else {
                            isAdmin = false
                            Log.d("LoginActivity", "User_ListActivity 실행 (사용자 계정)")
                            startActivity(Intent(this, User_ListActivity::class.java))
                            finish()
                        }
                    }
                }
            }
            // 로그인 실패
            else {
                Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
