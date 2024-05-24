package com.company.boogie.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.UserManager
import com.company.boogie.utils.FirebaseUserUtil
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        redirectActivity() // 로그인 정보에 따라 액티비티 실행
    }

    // 로그인 정보에 따라 액티비티 실행
    private fun redirectActivity() {
        val currentUser: FirebaseUser? = FirebaseUserUtil.whoAmI() // 로그인 정보
        Log.d("MainActivity", "로그인 정보: [uid]${currentUser?.uid}, [email]${currentUser?.email}")

        // 로그인 x - LoginActivity 실행
        if (currentUser == null) {
            Log.d("MainActivity", "LoginActivity 실행 (로그인 x)")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        // 로그인 o - 관리자면 Manager_ListActivity, 사용자면 User_ListActivity 실행
        else {
            val uid: String = currentUser.uid // 로그인 계정의 uid

            // uid로 DB에서 사용자 정보 가져옴
            FirebaseUserUtil.getUser(uid) { STATUS_CODE, user ->
                if (STATUS_CODE == StatusCode.SUCCESS && user != null) {
                    Log.d("MainActivity", "[${uid}]:사용자명[${user.name}]:관리자여부[(${user.isAdmin})] 사용자 정보 성공적으로 가져옴")

                    // 관리자 여부 저장
                    UserManager.isAdmin = user.isAdmin
                    Log.d("MainActivity", "isAdmin = ${UserManager.isAdmin}")

                    // 관리자 계정이면 Manager_ListActivity 실행
                    if (UserManager.isAdmin) {
                        Log.d("MainActivity", "Manager_ListActivity 실행 (로그인 o, 관리자 계정)")
                        startActivity(Intent(this, Manager_ListActivity::class.java))
                        finish()
                    }
                    // 사용자면 User_ListActivity 실행
                    else {
                        Log.d("MainActivity", "User_ListActivity 실행 (로그인 o, 사용자 계정)")
                        startActivity(Intent(this, User_ListActivity::class.java))
                        finish()
                    }
                }
                else {
                    Log.w("MainActivity", "[${uid}] 사용자 정보 가져오기 실패")
                }
            }
        }
    }

}