package com.company.boogie.utils

import com.company.boogie.StatusCode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseRequestUtil {

    /**
     * Product컬렉션(대여가능기자재)에 있는 문서를 Borrowing컬렉션(대여중인 기자재)로 이동시킵니다
     * @param productId 사용자가 대여할 기자재의 이름
     *
     * User컬렉션에 borrowing필드에 대여할 기자재를 삽입합니다.
     * 이미 대여중인 기자재가 있으면 "기자재를 대여할 수 없습니다(대여 중)"를 출력(터미널 상에 출력)
     *
     */
    fun productToBorrowing(productId: String, callback: (Int) -> Unit) {
        val db = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return callback(StatusCode.FAILURE)

        db.collection("User").whereEqualTo("email", currentUserEmail).get().addOnSuccessListener { userDocuments ->
            if (userDocuments.documents.isNotEmpty()) {
                val userDoc = userDocuments.documents.first()
                // 사용자가 이미 대여 중이면 FAILURE 콜백
                if (userDoc.getString("borrowing") != null) {
                    println("기자재를 대여할 수 없습니다(대여 중)")
                    callback(StatusCode.FAILURE)
                } else {
                    db.collection("Product").whereEqualTo("name", productId).get().addOnSuccessListener { productDocuments ->
                        if (productDocuments.documents.isNotEmpty()) {
                            val productDoc = productDocuments.documents.first()

                            //Borrowing 컬렉션으로 이동
                            val borrowingData = productDoc.data
                            db.collection("Borrowing").add(borrowingData!!).addOnSuccessListener {
                                // Product 컬렉션에 존재하던 기존 문서 삭제
                                productDoc.reference.delete()

                                // 사용자의 borrowing 필드 업데이트
                                userDoc.reference.update("borrowing", productId).addOnSuccessListener {
                                    callback(StatusCode.SUCCESS)
                                }.addOnFailureListener {
                                    println("사용자 대여 상태 업데이트 실패")
                                    callback(StatusCode.FAILURE)
                                }
                            }.addOnFailureListener {
                                println("제품을 Borrowing 컬렉션으로 이동 실패")
                                callback(StatusCode.FAILURE)
                            }
                        } else {
                            println("지정된 이름의 제품을 찾을 수 없음")
                            callback(StatusCode.FAILURE)
                        }
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


    /**
     *  Borrowing컬렉션(대여중인 기자재)에 있는 문서를 Product컬렉션(대여가능기자재)로 이동시킵니다
     *  @param productId 사용자가 반납할 기자재의 이름
     *
     *  User컬렉션의 borrowing필드를 null값으로 변경합니다(반납).
     *  이미 대여중인 기자재가 있으면 "반납할 수 없습니다(대여중인 기자재 없음)"를 출력(터미널 상에 출력)
     *
    */
    fun borrowingToProduct(productId: String, callback: (Int) -> Unit) {
        val db = Firebase.firestore
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return callback(StatusCode.FAILURE)

        db.collection("User").whereEqualTo("email", currentUserEmail).get().addOnSuccessListener { userDocuments ->
            if (userDocuments.documents.isNotEmpty()) {
                val userDoc = userDocuments.documents.first()
                // 사용자가 대여 중인 기자재가 없을 경우
                if (userDoc.getString("borrowing") == null) {
                    println("반납할 수 없습니다(대여중인 기자재 없음)")
                    callback(StatusCode.FAILURE)
                } else {
                    // Borrowing 컬렉션에서 Product ID에 해당하는 문서 가져오기
                    db.collection("Borrowing").whereEqualTo("name", productId).get().addOnSuccessListener { borrowingDocuments ->
                        if (borrowingDocuments.documents.isNotEmpty()) {
                            val borrowingDoc = borrowingDocuments.documents.first()

                            // Product 컬렉션으로 문서 이동
                            val productData = borrowingDoc.data
                            db.collection("Product").add(productData!!).addOnSuccessListener {
                                // Borrowing 컬렉션에서 기존 문서 삭제
                                borrowingDoc.reference.delete()

                                // 사용자의 borrowing 필드 null로 업데이트
                                userDoc.reference.update("borrowing", null).addOnSuccessListener {
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