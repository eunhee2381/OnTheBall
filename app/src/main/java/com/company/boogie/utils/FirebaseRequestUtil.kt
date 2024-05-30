package com.company.boogie.utils

import android.util.Log
import com.company.boogie.StatusCode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseRequestUtil {

    /**
     * Product컬렉션(대여가능기자재)에 있는 문서를 Borrowing컬렉션(대여중인 기자재)로 이동시킵니다
     * @param productName 사용자가 대여할 기자재의 이름
     * @param productId 사용자가 대여할 기자재의 번호
     *
     * User컬렉션에 borrowing필드에 대여할 기자재를 삽입합니다.
     * 이미 대여중인 기자재가 있으면 "기자재를 대여할 수 없습니다(대여 중)"를 출력(터미널 상에 출력)
     *
     * borrowing 필드를 업데이트 할 때 사용자의 borrowAt필드에 대여 시작 시간 업데이트
     *
     * 사용자 문서의 borrowed 서브 컬렉션에 대여한 기자재의 이름을 가진 문서에 대여한 시간 업데이트
     * (대여 기록 표시할 때 사용)
     *
     */
    public fun productToBorrowing(productName: String, productId: Int, selectedDate: String, callback: (Int) -> Unit) {
        val db = Firebase.firestore
        val currentUserEmail =
            FirebaseAuth.getInstance().currentUser?.email ?: return callback(StatusCode.FAILURE)

        // 현재 시간을 문자열로 포맷팅
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(System.currentTimeMillis())

        db.collection("User").whereEqualTo("email", currentUserEmail).get()
            .addOnSuccessListener { userDocuments ->
                if (userDocuments.documents.isNotEmpty()) {
                    val userDoc = userDocuments.documents.first()
                    // 사용자가 이미 다른 제품을 대여중인지 확인
                    if (userDoc.getString("borrowing").isNullOrBlank()) { // borrowing 필드가 공백인지 확인
                        db.collection("Product").whereEqualTo("name", productName).whereEqualTo("productId", productId).get()
                            .addOnSuccessListener { productDocuments ->
                                if (productDocuments.documents.isNotEmpty()) {
                                    val productDoc = productDocuments.documents.first()
                                    val borrowingData = productDoc.data
                                    db.collection("Borrowing").add(borrowingData!!)
                                        .addOnSuccessListener {
                                            productDoc.reference.delete()
                                            val userUpdates = hashMapOf<String, Any>(
                                                "borrowing" to productName
                                            )
                                            userDoc.reference.update(userUpdates).addOnSuccessListener {
                                                val borrowedData = hashMapOf(
                                                    "when" to currentDate, // 현재 시간을 문자열로 저장
                                                    "until" to selectedDate,
                                                    "name" to productName // 문서 내에 제품 이름 저장
                                                )
                                                userDoc.reference.collection("Borrowed")
                                                    .add(borrowedData) // 문서 ID는 자동 생성
                                                    .addOnSuccessListener {
                                                        callback(StatusCode.SUCCESS)
                                                    }.addOnFailureListener {
                                                        Log.e("ProductBorrowing", "Borrowed 문서 생성 실패")
                                                        callback(StatusCode.FAILURE)
                                                    }
                                            }.addOnFailureListener {
                                                Log.e("ProductBorrowing", "사용자 borrowing 필드 업데이트 실패")
                                                callback(StatusCode.FAILURE)
                                            }
                                        }.addOnFailureListener {
                                            Log.e("ProductBorrowing", "제품을 Borrowing 컬렉션으로 이동 실패")
                                            callback(StatusCode.FAILURE)
                                        }
                                } else {
                                    Log.e("ProductBorrowing", "지정된 이름과 ID의 제품을 찾을 수 없음")
                                    callback(StatusCode.FAILURE)
                                }
                            }
                    } else {
                        Log.e("ProductBorrowing", "대여를 신청할 수 없습니다(대여중)")
                        callback(StatusCode.FAILURE)
                    }
                } else {
                    Log.e("ProductBorrowing", "현재 이메일로 등록된 사용자를 찾을 수 없음")
                    callback(StatusCode.FAILURE)
                }
            }
    }







    /**
     *  Borrowing컬렉션(대여중인 기자재)에 있는 문서를 Product컬렉션(대여가능기자재)로 이동시킵니다
     *  @param productId 사용자가 반납할 기자재의 이름
     *
     *  User컬렉션의 borrowing필드를 null값으로 변경합니다(반납).
     *  이미 대여중인 기자재가 있으면 "반납할 수 없습니다(대여중인 기자재 없음)"를 출력(터미널 상에 출력)
     *
    */
    public fun borrowingToProduct(productName: String, productId: Int, callback: (Int) -> Unit) {
        val db = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return callback(StatusCode.FAILURE)

        db.collection("User").whereEqualTo("email", currentUserEmail).get().addOnSuccessListener { userDocuments ->
            if (userDocuments.documents.isNotEmpty()) {
                val userDoc = userDocuments.documents.first()
                // 사용자가 대여 중인 기자재가 없을 경우
                // Borrowing 컬렉션에서 Product ID에 해당하는 문서 가져오기
                db.collection("Borrowing").whereEqualTo("name", productName).whereEqualTo("productId", productId).get().addOnSuccessListener { borrowingDocuments ->
                    if (borrowingDocuments.documents.isNotEmpty()) {
                        val borrowingDoc = borrowingDocuments.documents.first()

                        // Product 컬렉션으로 문서 이동
                        val productData = borrowingDoc.data
                        db.collection("Product").add(productData!!).addOnSuccessListener {
                            // Borrowing 컬렉션에서 기존 문서 삭제
                            borrowingDoc.reference.delete()

                            // 사용자의 borrowing 필드 null로 업데이트
                            userDoc.reference.update("borrowing", "").addOnSuccessListener {
                                callback(StatusCode.SUCCESS)
                            }.addOnFailureListener {
                                println("사용자 반납 상태 업데이트 실패")
                                callback(StatusCode.FAILURE)
                            }
                        }.addOnFailureListener {
                            println("제품을 Product 컬렉션으로 이동 실패")
                            callback(StatusCode.FAILURE)
                        }
                    } else {
                        println("지정된 ID의 제품을 찾을 수 없음")
                        callback(StatusCode.FAILURE)
                    }
                }
            } else {
                println("현재 이메일로 등록된 사용자를 찾을 수 없음")
                callback(StatusCode.FAILURE)
            }
        }.addOnFailureListener {
            println("사용자 데이터 검색 실패")
            callback(StatusCode.FAILURE)
        }
    }


}