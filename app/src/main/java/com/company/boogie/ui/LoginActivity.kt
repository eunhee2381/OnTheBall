package com.company.boogie.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import com.company.boogie.StatusCode
import com.company.boogie.utils.FirebaseUserUtil

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)  // login 레이아웃을 불러옵니다.

        // 알람 권한 부여 - 앱 첫 실행시
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestSinglePermission(Manifest.permission.POST_NOTIFICATIONS)
        }

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
                            Log.d("LoginActivity", "Manager_ListActivity 실행 (관리자 계정)")
                            startActivity(Intent(this, Manager_ListActivity::class.java))
                            finish()
                        }
                        // 사용자면 User_ListActivity 실행
                        else {
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

    // 알람 권한 부여 함수 - 앱 첫 시작시 수락/거절 창 뜸
    private fun requestSinglePermission(permission: String) { // 한번에 하나의 권한만 요청하는 예제
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) // 권한 유무 확인
            return
        val requestPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { // 권한 요청 컨트랙트
                if (it == false) { // permission is not granted!
                    AlertDialog.Builder(this).apply {
                        setTitle("Warning")
                        setMessage(getString(R.string.no_permission, permission))
                    }.show()
                }
            }
        if (shouldShowRequestPermissionRationale(permission)) { // 권한 설명 필수 여부 확인
            // you should explain the reason why this app needs the permission.
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage(getString(R.string.req_permission_reason, permission))
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        } else {
            // should be called in onCreate()
            requestPermLauncher.launch(permission) // 권한 요청 시작
        }
    }
}
