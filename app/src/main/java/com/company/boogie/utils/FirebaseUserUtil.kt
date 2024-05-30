package com.company.boogie.utils

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.company.boogie.StatusCode
import com.company.boogie.models.FirestoreUserModel
import com.company.boogie.models.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firebase 인증 관련 작업을 처리하는 유틸리티 singleton 객체입니다.
 */
object FirebaseUserUtil {
    private val userModel = FirestoreUserModel()

    /**
     * FCM 토큰을 업데이트합니다.
     */
    fun updateTokenForUser(userEmail: String, callback: (Int) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newToken = task.result
                Log.d("UpdateToken", "New FCM token: $newToken")

                val db = FirebaseFirestore.getInstance()
                db.collection("User").whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Log.w("UpdateToken", "해당 이메일을 가진 사용자가 없습니다: $userEmail")
                            callback(StatusCode.FAILURE)
                            return@addOnSuccessListener
                        }
                        for (document in documents) {
                            db.collection("User").document(document.id)
                                .update("token", newToken)
                                .addOnSuccessListener {
                                    Log.d("UpdateToken", "Firestore에 FCM 토큰이 성공적으로 업데이트되었습니다.")
                                    callback(StatusCode.SUCCESS)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("UpdateToken", "Firestore에 FCM 토큰 업데이트 중 오류 발생", e)
                                    callback(StatusCode.FAILURE)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateToken", "사용자 문서를 가져오는 중 오류 발생: $userEmail", e)
                        callback(StatusCode.FAILURE)
                    }
            } else {
                Log.w("UpdateToken", "새 FCM 토큰을 가져오는 데 실패했습니다.", task.exception)
                callback(StatusCode.FAILURE)
            }
        }
    }

    fun handleResult(statusCode: Int) {
        if (statusCode == StatusCode.SUCCESS) {
            println("토큰 업데이트 성공!")
        } else {
            println("토큰 업데이트 실패.")
        }
    }

    /**
     * 이메일과 비밀번호를 사용하여 사용자 로그인을 수행합니다.
     */
    fun doSignIn(userEmail: String, password: String, callback: (Int, String?) -> Unit) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = Firebase.auth.currentUser?.uid.toString()
                    Log.d("FirebaseLoginUtils", "[${uid}] 사용자 로그인 성공")
                    callback(StatusCode.SUCCESS, uid)
                } else {
                    Log.w("FirebaseLoginUtils", "EMAIL[${userEmail}] 사용자 로그인 실패!!! -> ", task.exception)
                    callback(StatusCode.FAILURE, null)
                }
            }
    }

    /**
     * 새로운 사용자를 등록합니다.
     */
    fun doSignUp(userEmail: String, password: String, name: String, birth: String, userId: String, isAdmin: Boolean, borrowAt: Date, callback: (Int, String?) -> Unit) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener { additionTask ->
                if (additionTask.isSuccessful) {
                    val uid = Firebase.auth.currentUser?.uid.toString()
                    val currentUser = Firebase.auth.currentUser
                    val birthDate: Timestamp = parseBirthdayToTimestamp(birth)
                    val id = userId.toInt()
                    Log.d("FirebaseLoginUtils", "DB에 추가할 계정 정보: uid[$uid] name[$name] birthday[$birthDate] studentID[$id] isAdmin[$isAdmin]")
                    val newUser = User(
                        email = userEmail,
                        name = name,
                        birthday = birthDate,
                        studentID = id,
                        isAdmin = isAdmin,
                        isBanned = false,
                        borrowing = "",
                        token = "",
                        borrowAt = borrowAt
                    )
                    userModel.insertUser(uid, newUser) { STATUS_CODE ->
                        if (STATUS_CODE == StatusCode.SUCCESS) {
                            updateTokenForUser(userEmail, ::handleResult)
                            callback(StatusCode.SUCCESS, uid)
                        } else {
                            currentUser?.delete()
                                ?.addOnCompleteListener { deletionTask ->
                                    if (deletionTask.isSuccessful) {
                                        Log.d("FirebaseLoginUtils", "[${uid}] 계정 DB 정보 추가 실패로 인한 Auth 계정 삭제 성공")
                                    } else {
                                        Log.w("FirebaseLoginUtils", "[${uid}] 계정 DB 정보 추가 실패로 인한 Auth 계정 삭제 실패!!! -> ", deletionTask.exception)
                                    }
                                }
                            callback(StatusCode.FAILURE, null)
                        }
                    }
                } else {
                    Log.w("FirebaseLoginUtils", "Auth에서 사용자 생성 중 에러 발생!!! -> ", additionTask.exception)
                    callback(StatusCode.FAILURE, null)
                }
            }
    }

    /**
     * 생년월일 문자열을 Timestamp로 변환합니다.
     */
    fun parseBirthdayToTimestamp(birthday: String): Timestamp {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.parse(birthday)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Date()
        return Timestamp(calendar.time)
    }

    /**
     * 현재 로그인된 사용자를 로그아웃합니다.
     */
    fun doSignOut(callback: (Int) -> Unit) {
        val uid = whoAmI()?.uid
        Firebase.auth.signOut()
        if (Firebase.auth.currentUser == null) {
            Log.d("FirebaseLoginUtils", "[${uid}] 사용자 로그아웃 성공")
            callback(StatusCode.SUCCESS)
        } else {
            Log.w("FirebaseLoginUtils", "[${uid}] 사용자 로그아웃 실패...")
            callback(StatusCode.FAILURE)
        }
    }

    /**
     * UID를 사용하여 사용자 객체를 검색합니다.
     */
    fun getUser(uid: String, callback: (Int, User?) -> Unit) {
        userModel.getUserDetail(uid) { STATUS_CODE, user ->
            if (STATUS_CODE == StatusCode.SUCCESS) {
                callback(StatusCode.SUCCESS, user)
            } else {
                callback(StatusCode.FAILURE, null)
            }
        }
    }

    /**
     * 현재 로그인한 Firebase 사용자의 정보를 반환합니다.
     */
    fun whoAmI(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    /**
     * UID를 사용하여 사용자를 삭제합니다.
     */
    fun leaveUser(uid: String, callback: (Int) -> Unit) {
        if (Firebase.auth.currentUser == null) {
            Log.w("FirebaseLoginUtils", "[${uid}] 로그인 정보를 불러오지 못해 탈퇴 실패")
            callback(StatusCode.FAILURE)
        } else {
            userModel.deleteUser(uid) { STATUS_CODE ->
                if (STATUS_CODE == StatusCode.SUCCESS) {
                    Firebase.auth.currentUser!!.delete()
                        .addOnCompleteListener {
                            Log.d("FirebaseLoginUtils", "[${uid}] 계정 삭제 후 탈퇴 시도")
                            if (it.isSuccessful) {
                                Log.d("FirebaseLoginUtils", "[${uid}] 계정 삭제 후 탈퇴 성공")
                                callback(StatusCode.SUCCESS)
                            } else {
                                Log.w("FirebaseLoginUtils", "[${uid}] 계정 삭제 실패")
                                callback(StatusCode.FAILURE)
                            }
                        }
                } else {
                    callback(StatusCode.FAILURE)
                }
            }
        }
    }
}
