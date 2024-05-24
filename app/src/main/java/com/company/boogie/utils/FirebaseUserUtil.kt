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

/**
 * Firebase 인증 관련 작업을 처리하는 유틸리티 singleton 객체입니다.
 */
object FirebaseUserUtil{

    private val userModel = FirestoreUserModel()

    /**
     * 이메일과 비밀번호를 사용하여 사용자 로그인을 수행합니다.
     *
     * @param userEmail 로그인할 사용자의 이메일입니다.
     * @param password 로그인할 사용자의 비밀번호입니다.
     * @param callback 로그인 성공 시 상태 코드(STATUS_CODE)와 사용자 ID를 인자로 받는 콜백 함수입니다.
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
     * 이메일, 비밀번호, 이름, 생년월일을 사용하여 새로운 사용자를 등록합니다.
     *
     * @param userEmail 새로운 사용자의 이메일입니다.
     * @param password 새로운 사용자를 위한 비밀번호입니다.
     * @param name 새로운 사용자의 이름입니다.
     * @param birth 새로운 사용자의 생년월일로 "yyyy-MM-dd" 형식입니다.
     * @param userId 새로운 사용자의 학번입니다.
     * @param isAdmin 새로운 사용자의 관리자 여부입니다.
     *
     * @param callback 회원가입 성공 시 상태 코드(STATUS_CODE)와 사용자 ID를 인자로 받는 콜백 함수입니다.
     */
    fun doSignUp(userEmail: String, password: String, name: String, birth: String, userId: String, isAdmin: Boolean, callback: (Int, String?) -> Unit){
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
                        token = ""
                    )
                    userModel.insertUser(uid, newUser) { STATUS_CODE ->
                        if (STATUS_CODE == StatusCode.SUCCESS) {
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
     * 생년월일 문자열을 Firebase에 저장할 수 있는 Timestamp로 변환합니다.
     *
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
     *
     * @param callback 로그아웃 결과를 나타내는 상태 코드(STATUS_CODE)를 인자로 받는 콜백 함수입니다.
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
     *
     * @param uid 검색할 사용자의 UID입니다.
     * @param callback 사용자 정보 조회 성공 시 상태 코드(STATUS_CODE)와 사용자 객체를 인자로 받는 콜백 함수입니다.
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
     *
     * @return FirebaseUser 객체로 현재 로그인한 사용자의 정보를 반환하거나, 로그인한 사용자가 없을 경우 null을 반환합니다.
     */
    fun whoAmI(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    /**
     * UID를 사용하여 사용자를 삭제합니다.
     *
     * @param uid 검색할 사용자의 UID입니다.
     */
    fun leaveUser(uid: String, callback: (Int) -> Unit) {
        if (Firebase.auth.currentUser == null) {
            Log.w("FirebaseLoginUtils", "[${uid}] 로그인 정보를 불러오지 못해 탈퇴 실패")
            callback(StatusCode.FAILURE)
        }
        // DB에서 계정 삭제 후 Auth에서 계정 삭제
        else {
            userModel.deleteUser(uid) { STATUS_CODE ->
                // DB에서 계정 삭제 성공
                if (STATUS_CODE == StatusCode.SUCCESS) {
                    Firebase.auth.currentUser!!.delete()
                        .addOnCompleteListener {
                            Log.d("FirebaseLoginUtils", "[${uid}] 계정 삭제 후 탈퇴 시도")
                            // Auth에서 계정 삭제 성공
                            if (it.isSuccessful) {
                                Log.d("FirebaseLoginUtils", "[${uid}] 계정 삭제 후 탈퇴 성공")
                                callback(StatusCode.SUCCESS)
                            }
                            // Auth에서 계정 삭제 실패
                            else {
                                Log.w("FirebaseLoginUtils", "[${uid}] 계정 삭제 실패")
                                callback(StatusCode.FAILURE)
                            }
                        }
                }
                // DB에서 계정 삭제 실패
                else {
                    callback(StatusCode.FAILURE)
                }
            }
        }
    }
}