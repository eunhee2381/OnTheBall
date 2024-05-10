package com.company.boogie.utils

import android.app.AlertDialog
import android.app.Notification
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import com.company.boogie.models.FirestoreMessagingModel


class FirebaseRequestUtil {

    /**
     * 사용자의 이메일(아이디)와 기자재 이름을 사용하여 대여 요청을 수행합니다
     *
     * @param userEmail 계정을 구분하는데 사용되는 신청자의 이메일입니다.
     * @param productName 신청할 기자재의 이름입니다.
     * @param callback  요청 성공시 상태 코드(STATUS_CODE)를 인자로 받는 콜백 함수입니다.
     */
    fun doRequest(userEmail: String, productName: String) {
        val db = FirebaseFirestore.getInstance()

        // Firestore에서 isAdmin이 true인 모든 사용자를 조회
        db.collection("User")
            .whereEqualTo("isAdmin", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // 각 사용자의 토큰을 얻어 알림 보내기
                    val token = document.getString("token") ?: continue  // 각 사용자 문서에 토큰이 저장되어 있다고 가정
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

}