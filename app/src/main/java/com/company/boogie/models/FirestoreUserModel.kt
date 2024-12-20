package com.company.boogie.models

import android.util.Log
import com.company.boogie.StatusCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Firestore를 사용하여 사용자 데이터를 관리하는 모델 클래스입니다.
 */
class FirestoreUserModel {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Firestore에 사용자를 추가합니다.
     *
     * @param uid 사용자의 고유 ID입니다.
     * @param user 사용자 정보를 담고 있는 User 객체입니다.
     * @param callback 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun insertUser(uid: String, user: User, callback: (Int) -> Unit) {
        db.collection("User").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("FirestoreUserModel", "[${uid}]: 사용자명[${user.name}], 사용자이메일[(${user.email})] 사용자 DB 정보 성공적으로 생성")
                callback(StatusCode.SUCCESS)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "[${uid}] 사용자 DB 정보 생성 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE)
            }
    }

    /**
     * Firestore에서 특정 사용자를 삭제합니다.
     *
     * @param uid 사용자의 고유 ID입니다.
     * @param callback 사용자 정보 조회 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun deleteUser(uid: String, callback: (Int) -> Unit) {
        db.collection("User").document(uid).delete()
            .addOnSuccessListener {
                Log.d("FirestoreUserModel", "[${uid}] 사용자 DB 정보 성공적으로 삭제")
                callback(StatusCode.SUCCESS)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "[${uid}] 사용자 DB 정보 삭제 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE)
            }
    }

    /**
     * Firestore에서 특정 사용자의 상세 정보를 조회합니다.
     *
     * @param uid 상세 정보를 조회할 사용자의 고유 ID입니다.
     * @param callback 사용자 정보 조회 상태 코드(STATUS_CODE)와 User 객체를 반환하는 콜백 함수입니다.
     */
    fun getUserDetail(uid: String, callback: (Int, User?) -> Unit) {
        db.collection("User").document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        val user = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            Log.d("FirestoreUserModel", "[${uid}] 사용자 DB에 불러오기 성공")
                            callback(StatusCode.SUCCESS, user)
                        } else {
                            Log.d("FirestoreUserModel", "[${uid}] 사용자 데이터를 변환할 수 없습니다.")
                            callback(StatusCode.FAILURE, null)
                        }
                    } catch (e: Exception) {
                        Log.e("FirestoreUserModel", "[${uid}] 사용자 데이터 변환 중 에러 발생: ", e)
                        callback(StatusCode.FAILURE, null)
                    }
                } else {
                    Log.d("FirestoreUserModel", "[${uid}] 사용자가 DB에 존재하지 않음")
                    callback(StatusCode.FAILURE, null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "[${uid}] 사용자를 DB에서 불러오는 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE, null)
            }
    }

    /**
     * 현재 로그인 중인 계정이 받은 알람을 불러오는 함수
     */
    public fun getAlarms(callback: (Int, List<Message>?) -> Unit) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email // 현재 로그인한 사용자의 이메일 가져오기

        if (userEmail == null) {
            Log.w("FirestoreUserModel", "사용자 인증 정보가 없습니다.")
            callback(StatusCode.FAILURE, null)
            return
        }

        db.collection("User")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreUserModel", "해당 이메일을 가진 사용자 문서가 없습니다.")
                    callback(StatusCode.SUCCESS, null)
                } else {
                    // 일반적으로 이메일은 고유해야 하므로, 첫 번째 문서만 사용합니다.
                    val userDocument = documents.documents.first()
                    userDocument.reference.collection("messages")
                        .get()
                        .addOnSuccessListener { messageDocuments ->
                            val messages = messageDocuments.mapNotNull { document ->
                                // document.toObject(Message::class.java) 사용하여 각 문서를 Message 객체로 변환
                                document.toObject(Message::class.java)
                            }
                            Log.d("FirestoreUserModel", "메시지 목록을 성공적으로 불러왔습니다.")
                            callback(StatusCode.SUCCESS, messages)
                        }
                        .addOnFailureListener { e ->
                            Log.w("FirestoreUserModel", "메시지 컬렉션을 불러오는 중 에러 발생!!! -> ", e)
                            callback(StatusCode.FAILURE, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "User 컬렉션을 조회하는 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE, null)
            }
    }

    /**
     * 블랙리스트에 올라가있는 계정의 정보를 불러오는 함수
     *
     */
    fun getBlacklist(callback: (Int, List<User>?) -> Unit) {
        val db = Firebase.firestore  // Firestore 인스턴스

        db.collection("User")
            .whereEqualTo("isBanned", true)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreUserModel", "차단된 사용자가 없습니다.")
                    callback(StatusCode.SUCCESS, null)
                } else {
                    val bannedUsers = documents.mapNotNull { document ->
                        document.toObject(User::class.java)  // 각 문서를 User 객체로 변환
                    }
                    Log.d("FirestoreUserModel", "차단된 사용자 목록을 성공적으로 불러왔습니다.")
                    callback(StatusCode.SUCCESS, bannedUsers)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "User 컬렉션을 조회하는 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE, null)
            }
    }
    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        db.collection("User").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val user = documents.documents[0].toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "사용자 정보를 이메일로 불러오는 중 에러 발생!!! -> ", e)
                callback(null)
            }
    }

    fun updateUser(user: User, callback: (Int) -> Unit) {
        db.collection("User").document(user.email).set(user)
            .addOnSuccessListener {
                Log.d("FirestoreUserModel", "사용자 정보 업데이트 성공")
                callback(StatusCode.SUCCESS)
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreUserModel", "사용자 정보 업데이트 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE)
            }
    }



}
