package com.company.boogie.models

import android.os.Handler
import android.os.Looper
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirestoreMessagingModel : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 사용자 토큰을 Firestore에 저장
        val db = Firebase.firestore
        val userToken = hashMapOf("fcmToken" to token)
        db.collection("users").document("userId").set(userToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.isNotEmpty().let {
            val message = remoteMessage.data["message"]

            // 알림 메시지로 다이얼로그 표시
            showRequestDialog(message)
        }
    }

    private fun showRequestDialog(message: String?) {
        // UI thread에서 다이얼로그를 표시하려면 Handler 사용 또는 Activity 내에서 처리
        Handler(Looper.getMainLooper()).post {
            // AlertDialog.Builder 등을 사용하여 다이얼로그 생성 및 표시
            // '허락'과 '거절' 버튼 처리 로직 추가
        }
    }
}
