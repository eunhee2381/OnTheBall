package com.company.boogie.models

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.company.boogie.R
import com.company.boogie.ui.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification_channel"
const val channelName = "com.company.boogie"
class FirestoreMessagingModel : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("Hello", "onNewToken called with token: $token")

        // 사용자 토큰을 Firestore에 저장
        val db = Firebase.firestore
        val userToken = hashMapOf("fcmToken" to token)

        // Firestore에 저장
        db.collection("User").document("userId")
            .set(userToken)
            .addOnSuccessListener {
                Log.d("Hello", "FCM token successfully saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("Hello", "Error saving FCM token to Firestore", e)
            }

        // 로그캣에서 토큰 보기
        Log.d("Hello", "FCM token: $token")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 고유한 알림 ID 생성
        val notificationId = System.currentTimeMillis().toInt()

        // Notification 부분이 있는 경우
        remoteMessage.notification?.let {
            generateNotification(it.title ?: "No Title", it.body ?: "No Message", it.body ?: "No Message", notificationId)
        }

        // 로그캣에서 메세지 보기
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("Hello", "FCM message data: ${remoteMessage.data}")
        } else {
            Log.d("Hello", "FCM message has no data")
        }
    }
    /*
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.isNotEmpty().let {
            val message = remoteMessage.data["message"]

            // 알림 메시지로 다이얼로그 표시
            showRequestDialog(message)
        }
    }
    */

    @SuppressLint("RemoteViewLayout")
    private fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification)

        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)
        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.alarm)

        return remoteViews
    }


    /**
     * 알람을 보내는 함수입니다
     * @param title 알람 제목
     * @param userName 알람을 보낸 사람의 이름
     * @param productName 대여 신청할 기자재의 이름
     *      * @param message 알람 내용
     *
     * 알람 형식 -> ex) 태민(userName) 님이 아두이노(productName) 을 대여요청했습니다.
     */
    private fun generateNotification(title: String, userName: String, productName: String, notificationId: Int) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val message = userName + "님이" + productName + "을 대여요청하였습니다"

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationLayout = getRemoteView(title, message)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.alarm)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            //.setOnlyAlertOnce(true)       // 알림이 한 번만 알림음을 울리고 업데이트
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(notificationId, builder.build())
        //notificationManager.notify(0, builder.build())

        // Notification 생성 로직
        Log.d("Hello", "Notification generated with title: $title, message: $message")

    }
    /*
    private fun showRequestDialog(message: String?) {
        // UI thread에서 다이얼로그를 표시하려면 Handler 사용 또는 Activity 내에서 처리
        Handler(Looper.getMainLooper()).post {
            // AlertDialog.Builder 등을 사용하여 다이얼로그 생성 및 표시
            // '허락'과 '거절' 버튼 처리 로직 추가
        }
    }
    */

}
