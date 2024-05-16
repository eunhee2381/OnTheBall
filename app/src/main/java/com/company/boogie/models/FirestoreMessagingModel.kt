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
        // 사용자 토큰을 Firestore에 저장
        val db = Firebase.firestore
        val userToken = hashMapOf("fcmToken" to token)
        db.collection("users").document("userId").set(userToken)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            generateNotification(it.title ?: "No Title", it.body ?: "No Message")
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

    private fun generateNotification(title: String, message: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationLayout = getRemoteView(title, message)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.alarm)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())
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
