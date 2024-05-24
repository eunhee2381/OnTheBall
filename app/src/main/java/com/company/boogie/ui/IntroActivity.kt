package com.company.boogie.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro) // xml, Kotlin 소스 연결

        // 앱 첫 실행시 - 알림 권한 부여
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestSinglePermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

//    override fun onPause() {
//        super.onPause()
//        finish()
//    }

    // 알람 권한 부여 함수 - 앱 첫 시작시 수락/거절 창 뜸
    // 알림 권한 창이 사라지면, 1초 동안 인트로 실행 후 MainActivity로 전환
    private fun requestSinglePermission(permission: String) { // 한번에 하나의 권한만 요청하는 예제
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) { // 권한 유무 확인
            Log.d("IntroActivity", "권한이 이미 있음")
            startMainAfterIntro()
            return
        }
        val requestPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { // 권한 요청 컨트랙트
                if (it == false) { // permission is not granted!
                    Log.d("IntroActivity", "권한 거절 시 다이얼로그")
                    AlertDialog.Builder(this).apply {
                        setTitle("Warning")
                        setMessage(getString(R.string.no_permission, permission))
                    }.show().setOnDismissListener {
                        startMainAfterIntro()
                    }
                }
                else {
                    Log.d("IntroActivity", "권한 수락")
                    startMainAfterIntro()
                }
            }
        if (shouldShowRequestPermissionRationale(permission)) { // 권한 설명 필수 여부 확인
            // you should explain the reason why this app needs the permission.
            Log.d("IntroActivity", "권한 설명 다이얼로그")
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage(getString(R.string.req_permission_reason, permission))
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ ->  startMainAfterIntro() }
            }.show()
        } else {
            // should be called in onCreate()
            Log.d("IntroActivity", "권한 요청")
            requestPermLauncher.launch(permission) // 권한 요청 시작
        }
    }

    // 1초 동안 인트로 실행 후 MainActivity로 넘어감
    private fun startMainAfterIntro() {
        Log.d("IntroActivity", "1초 동안 인트로 실행 후 메인액티비티로 넘어감")
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent) // 인트로 실행 후 바로 MainActivity로 넘어감.
            finish()
        }, 1000) // 1초 동안 인트로 실행
    }

}
