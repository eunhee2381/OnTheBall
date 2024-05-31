package com.company.boogie.ui

import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.company.boogie.R
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class Manager_Notification_DialogActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 바 제거
        setContentView(R.layout.manager_notification_dialog)

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAccept: Button = findViewById(R.id.btn_accept)
        val btnRefuse: Button = findViewById(R.id.btn_refuse)

        val requesterEmail = intent.getStringExtra("requesterEmail") ?: "" // 요청자 이메일 받기

        btnAccept.setOnClickListener {
            sendNotification(requesterEmail, true) // 수락 알림 보내기
            finish() // 다이얼로그 닫기
        }

        btnRefuse.setOnClickListener {
            sendNotification(requesterEmail, false) // 거절 알림 보내기
            finish() // 다이얼로그 닫기
        }
    }

    private fun sendNotification(email: String, isAccepted: Boolean) {
        val url = "http://localhost:3000" // 실제 서버의 URL로 변경 필요
        val mediaType = "application/json; charset=utf-8".toMediaType() // 수정된 부분
        val title = if (isAccepted) "대여 요청 승인" else "대여 요청 거부"
        val message = if (isAccepted) "$email 사용자의 대여 요청이 허가되었습니다." else "$email 사용자의 대여 요청이 거부되었습니다."

        val body = """
        {
            "email": "$email",
            "title": "$title",
            "message": "$message"
        }
    """.trimIndent()

        val requestBody = body.toRequestBody(mediaType) // 수정된 부분
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // 오류 발생 시 스택 트레이스를 출력
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        throw IOException("Unexpected code $resp")
                    }
                    // 서버 응답을 안전하게 처리하고 로그 출력
                    val responseBody = resp.body?.string() // responseBody는 nullable이므로 안전 호출 사용
                    println("Server response: $responseBody")
                }
            }
        })

    }
}
