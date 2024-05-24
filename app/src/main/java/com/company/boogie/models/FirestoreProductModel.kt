package com.company.boogie.models

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.company.boogie.StatusCode
import com.google.firebase.firestore.Query
import android.net.http.UrlRequest.Status
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FirestoreProductModel {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Firestore에서 특정 ID의 기자재 상세 정보를 조회합니다.
     *
     * @param productName 상세 정보를 조회할 기자재의 이름입니다.
     * @param callback 사용자 정보 조회 상태 코드(STATUS_CODE)와 User 객체를 반환하는 콜백 함수입니다.
     */
    fun getProductsById(productName: String, callback: (Int, Product?) -> Unit) {
        val productsRef = db.collection("Product")
        productsRef.whereEqualTo("name", productName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // name 필드가 일치하는 첫 번째 문서를 가져옵니다.
                    val documentSnapshot = querySnapshot.documents.first()
                    Log.d("FirestoreProductModel", "[$productName] 기자재 조회 성공")
                    val product = documentSnapshot.toObject(Product::class.java)
                    callback(StatusCode.SUCCESS, product)
                } else {
                    Log.d("FirestoreProductModel", "[$productName] 기자재 DB에 존재하지 않음")
                    callback(StatusCode.FAILURE, null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "[$productName] 기자재를 DB에서 불러오는 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE, null)
            }
    }

    /**
     * Firestore에서 모든 기자재의 상세 정보를 실시간으로 조회합니다.
     *
     * @param callback 기자재 정보 조회 상태 코드(STATUS_CODE)와 기자재 리스트를 반환하는 콜백 함수입니다.
     */
    fun getProducts(callback: (Int, List<Product>?) -> Unit) {
        db.collection("Product")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w("FirestoreProductModel", "전체 기자재 목록을 DB에서 불러오는 중 에러 발생!!! -> ", e)
                    callback(StatusCode.FAILURE, null)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val productList = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Product::class.java)
                    }
                    Log.d("FirestoreProductModel", "전체 기자재 목록 DB에서 불러오기 성공")
                    callback(StatusCode.SUCCESS, productList)
                } else {
                    Log.d("FirestoreProductModel", "기자재 목록이 DB에 존재하지 않음")
                    callback(StatusCode.SUCCESS, null)
                }
            }
    }

    /**
     * Firestore에서 특정 기자재을 삭제합니다.
     *
     * @param productName 기자재의 이름입니다.
     * @param callback 기자재 삭제 결과에 대한 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun deleteProduct(productName: String, callback: (Int) -> Unit) {
        // Product 컬렉션에서 name 필드가 productName과 일치하는 문서 찾기
        db.collection("Product").whereEqualTo("name", productName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreProductModel", "[$productName] 해당 제품을 찾을 수 없음")
                    callback(StatusCode.FAILURE)
                } else {
                    // 일치하는 첫 번째 문서에 대해서만 처리
                    val productDoc = documents.documents.first()
                    val pid = productDoc.id
                    val imageRef = FirebaseStorage.getInstance().reference.child("Product/${pid}")

                    imageRef.delete()
                        .addOnSuccessListener {
                            productDoc.reference.delete()
                                .addOnSuccessListener {
                                    Log.d("FirestoreProductModel", "[$productName] 제품 정보 및 이미지 성공적으로 삭제")
                                    callback(StatusCode.SUCCESS)
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FirestoreProductModel", "[$productName] 제품 DB 정보 삭제 중 에러 발생!!! -> ", e)
                                    callback(StatusCode.FAILURE)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FirestoreProductModel", "[$productName] 제품 이미지 Storage에서 삭제 중 에러 발생!!! -> ", e)
                            callback(StatusCode.FAILURE)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "[$productName] 제품 검색 중 에러 발생!!! -> ", e)
                callback(StatusCode.FAILURE)
            }
    }


    /**
     * Firestore에서 기자재 정보를 수정합니다.
     *
     * @param productId 기자재의 고유 ID입니다.
     * @param updatedName 수정된 기자재의 이름입니다.
     * @param updateImage 수정된 기자재의 이미지 url입니다.
     * @param updatedDetail 수정된 기자재의 상세 정보입니다.
     * @param updatedLocation 수정된 기자재의 위치입니다.
     * @param updatedClassificationCode 수정된 기자재의 상태 코드입니다.
     * @param updatedProductId 수정된 기자재의 번호입니다.
     * @param callback 상품 정보 수정 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun updateProduct(productId: String, updatedName: String, updateImage: String?, updatedDetail: String, updatedLocation: String, updatedClassificationCode: Int, updatedProductId: String, callback: (Int) -> Unit) {
        val productRef = db.collection("Product").document(productId)
        val imageRef = FirebaseStorage.getInstance().reference.child("Product/${productId}")

        if(updateImage != null) {
            imageRef.metadata
                .addOnSuccessListener {
                    imageRef.delete().addOnSuccessListener {
                        imageRef.putFile(Uri.parse(updateImage))
                            .addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val updatedData = hashMapOf(
                                        "name" to updatedName,
                                        "img" to uri.toString(),
                                        "detail" to updatedDetail,
                                        "location" to updatedLocation,
                                        "classificationCode" to updatedClassificationCode,
                                        "productId" to updatedProductId
                                    )
                                    val updatedDataMap: Map<String, Any> = updatedData

                                    productRef.update(updatedDataMap)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "FirestoreProductModel",
                                                "Storage에 이미지 수정 후 기자재 수정 성공"
                                            )
                                            callback(StatusCode.SUCCESS)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "FirestoreProductModel",
                                                "Storage에 이미지 수정 후 기자재 수정 실패-> ",
                                                e
                                            )
                                            callback(StatusCode.FAILURE)
                                        }
                                }
                            }
                    }.addOnFailureListener { e ->
                        Log.w("FirestoreProductModel", "수정전 이미지 Storage에 삭제 실패-> ", e)
                    }
                }.addOnFailureListener {
                    imageRef.putFile(Uri.parse(updateImage))
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val updatedData = hashMapOf(
                                    "name" to updatedName,
                                    "img" to uri.toString(),
                                    "detail" to updatedDetail,
                                    "location" to updatedLocation,
                                    "classificationCode" to updatedClassificationCode,
                                    "productId" to updatedProductId
                                )
                                val updatedDataMap: Map<String, Any> = updatedData

                                productRef.update(updatedDataMap)
                                    .addOnSuccessListener {
                                        Log.d("FirestoreProductModel", "Storage에 이미지가 없던 기자재 수정 성공")
                                        callback(StatusCode.SUCCESS)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "FirestoreProductModel",
                                            "Storage에 이미지가 없던 기자재 수정 실패-> ",
                                            e
                                        )
                                        callback(StatusCode.FAILURE)
                                    }
                            }
                        }
                }
        } else {
            getProductsById(productId) { SUCCESS_CODE, product ->
                if(SUCCESS_CODE == StatusCode.SUCCESS){
                    val updatedData = hashMapOf(
                        "name" to updatedName,
                        "img" to (product?.img ?: ""),
                        "detail" to updatedDetail,
                        "location" to updatedLocation,
                        "classificationCode" to updatedClassificationCode,
                        "productId" to updatedProductId
                    )
                    val updatedDataMap: Map<String, Any> = updatedData

                    productRef.update(updatedDataMap)
                        .addOnSuccessListener {
                            Log.d(
                                "FirestoreProductModel",
                                "이미지 수정이 없던 기자재 수정 성공"
                            )
                            callback(StatusCode.SUCCESS)
                        }
                        .addOnFailureListener { e ->
                            Log.w(
                                "FirestoreProductModel",
                                "이미지 수정이 없던 기자재 수정 실패!!! -> ",
                                e
                            )
                            callback(StatusCode.FAILURE)
                        }
                } else {
                    Log.d("FirestoreProductModel", "이미지 수정이 없던 기자재 수정 중 상품 정보 불러오기 실패")
                    callback(StatusCode.FAILURE)
                }
            }
        }
    }

    /**
     * Firestore에서 isBanned 를 수정합니다.
     *
     * @param userEmail isBanned 상태를 변경할 이메일입니다.
     * @param callback 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun updateBanStatus(userEmail: String, isBanned: Boolean, callback: (Int) -> Unit) {
        // User 컬렉션에서 email 필드를 사용하여 문서 찾기
        db.collection("User")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreProductModel", "[$userEmail] 해당 이메일을 가진 사용자 없음")
                    callback(StatusCode.FAILURE)
                } else {
                    // 일치하는 첫 번째 문서에 대해서만 isBanned 필드 업데이트
                    val userDoc = documents.documents.first()
                    db.collection("User").document(userDoc.id)
                        .update("isBanned", isBanned)
                        .addOnSuccessListener {
                            Log.d("FirestoreProductModel", "[$userEmail] 사용자 밴 여부 성공적으로 변경 완료")
                            callback(StatusCode.SUCCESS)
                        }
                        .addOnFailureListener { e ->
                            Log.w("FirestoreProductModel", "[$userEmail] 사용자 밴 여부 변경중 오류 발생", e)
                            callback(StatusCode.FAILURE)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreProductModel", "[$userEmail] 사용자 검색 중 에러 발생", exception)
                callback(StatusCode.FAILURE)
            }
    }

}