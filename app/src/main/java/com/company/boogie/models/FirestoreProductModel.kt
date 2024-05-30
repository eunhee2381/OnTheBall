package com.company.boogie.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.company.boogie.StatusCode
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

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
     * Firestore에서 특정 기자재을 삭제합니다.
     *
     * @param documentId 조회를 위한 기자재 document의 ID입니다.
     * @param canBorrow true면 Product 컬렉션에서, false면 Borrowing 컬렉션에서 조회합니다.
     * @param callback 기자재 삭제 결과에 대한 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun deleteProductByDocumentId(documentId: String, canBorrow: Boolean, callback: (Int) -> Unit) {
        val collectionRef = if (canBorrow) db.collection("Product") else db.collection("Borrowing")
        collectionRef.document(documentId).delete()
            .addOnSuccessListener {
                Log.d("FirestoreProductModel", "기자재 삭제 성공 $documentId")
                callback(StatusCode.SUCCESS)
            }
            .addOnFailureListener {
                Log.w("FirestoreProductModel", "기자재 삭제 실패 $documentId")
                callback(StatusCode.FAILURE)
            }
    }

    /**
     * Firestore에 기자재를 추가합니다
     *
     * @param addName 기자재의 이름입니다.
     * @param addImage 기자재의 이미지 url입니다.
     * @param addDetail 기자재의 상세 정보입니다.
     * @param addLocation 기자재의 위치입니다.
     * @param addClassificationCode 기자재의 상태 코드입니다.
     * @param addProductId 기자재의 번호입니다.
     * @param callback 상품 정보 수정 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun addProduct(addProductId: Int, addName: String, addLocation: String, addImage: String, addDetail: String, addClassificationCode: Int, callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val newProduct = hashMapOf(
            "productId" to addProductId,
            "name" to addName,
            "location" to addLocation,
            "img" to addImage,
            "detail" to addDetail,
            "classificationCode" to addClassificationCode
        )

        db.collection("Product")
            .add(newProduct)
            .addOnSuccessListener { documentReference ->
                println("DocumentSnapshot added with ID: ${documentReference.id}")
                callback(StatusCode.SUCCESS) // 성공 콜백 호출
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
                callback(StatusCode.FAILURE) // 실패 콜백 호출
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
     * Firestore에서 기자재 정보를 수정합니다.
     *
     * @param documentId 조회를 위한 기자재 document의 ID입니다.
     * @param canBorrow true면 Product 컬렉션에서, false면 Borrowing 컬렉션에서 조회합니다.
     * @param updatedLocation 수정된 기자재의 위치입니다.
     * @param updatedDetail 수정된 기자재의 상세 정보입니다.
     * @param callback 상품 정보 수정 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun updateLocationDetail(documentId: String, canBorrow: Boolean, updatedLocation: String, updatedDetail: String, callback: (Int) -> Unit) {
        val collectionRef = if (canBorrow) db.collection("Product") else db.collection("Borrowing")

        var locationUpdateSuccess = false
        var detailUpdateSuccess = false

        // 위치 업데이트
        collectionRef.document(documentId).update("location", updatedLocation)
            .addOnSuccessListener {
                Log.d("FirestoreProductModel", "기자재 위치 업데이트 성공 - $documentId, $updatedLocation")
                locationUpdateSuccess = true
                if (locationUpdateSuccess && detailUpdateSuccess) {
                    callback(StatusCode.SUCCESS)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "기자재 위치 업데이트 실패 - $documentId, $updatedLocation", e)
                callback(StatusCode.FAILURE)
            }

        // 설명 업데이트
        collectionRef.document(documentId).update("detail", updatedDetail)
            .addOnSuccessListener {
                Log.d("FirestoreProductModel", "기자재 설명 업데이트 성공 - $documentId, $updatedDetail")
                detailUpdateSuccess = true
                if (locationUpdateSuccess && detailUpdateSuccess) {
                    callback(StatusCode.SUCCESS)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "기자재 설명 업데이트 실패 - $documentId, $updatedDetail", e)
                callback(StatusCode.FAILURE)
            }
    }

    /**
     * Storage에 이미지를 업로드하고 Firestore에 이미지 경로를 img 필드에 저장합니다.
     *
     * @param uri Storage에 저장할 이미지 Uri입니다.
     * @param classficationCode 기자재의 종류 코드입니다.
     * @param productId 종류 당 기자재의 id입니다.
     * @param documentId 조회를 위한 기자재 document의 ID입니다.
     * @param canBorrow true면 Product 컬렉션에서, false면 Borrowing 컬렉션에서 조회합니다.
     * @param callback 상태 코드(STATUS_CODE)를 반환하는 콜백 함수입니다.
     */
    fun updateImg(uri: Uri, classificatonCode: Int, productId: Int, documentId: String, canBorrow: Boolean, callback: (Int) -> Unit) {
        val storageRef = Firebase.storage.reference.child("Product/$classificatonCode/$productId.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener { _ ->
                Log.d("FirestoreProductModel", "이미지 storage에 업로드 성공")
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        Log.d("FirestoreProductModel", "storage 저장소 경로 알아오기 성공 $imageUrl")
                        val collectionRef = if (canBorrow) db.collection("Product") else db.collection("Borrowing")
                        collectionRef.document(documentId).update("img", imageUrl)
                            .addOnSuccessListener {
                                Log.d("FirestoreProductModel", "firestore img 필드에 storage 저장소 경로 업데이트 성공")
                                callback(StatusCode.SUCCESS)
                            }
                            .addOnFailureListener { e ->
                                Log.w("FirestoreProductModel", "firestore img 필드에 storage 저장소 경로 업데이트 실패", e)
                                callback(StatusCode.FAILURE)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.w("FirestoreProductModel", "storage 저장소 경로 알아오기 실패", e)
                        callback(StatusCode.FAILURE)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "이미지 storage에 업로드 실패", e)
                callback(StatusCode.FAILURE)
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

    /**
     * Firestore에서 productId가 1인 기자재 정보를 조회합니다.
     * RecyclerView에서 classificationCode 당 productId가 1인 기자재 리스트를 띄우기 위함
     *
     * @param callback 기자재 정보 조회 상태 코드(STATUS_CODE)와 기자재 리스트를 반환하는 콜백 함수입니다.
     */
    fun getProductsByProductId1(callback: (Int, List<Product>?) -> Unit) {
        val productsRef = db.collection("Product")
        val borrowingsRef = db.collection("Borrowing")

        val products = mutableListOf<Product>()
        val tasks = mutableListOf<Task<QuerySnapshot>>()

        // productId=1인 기자재를 Product, Borrowing 컬렉션에서 찾음
        tasks.add(productsRef.whereEqualTo("productId", 1).get())
        tasks.add(borrowingsRef.whereEqualTo("productId", 1).get())

        Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnSuccessListener { results ->
            Log.d("FirestoreProductModel", "productId=1인 기자재를 firestore에서 불러오기 성공")

            // 찾은 결과를 리스트에 넣음
            for (result in results) {
                for (document in result) {
                    val product = document.toObject<Product>()
                    products.add(product) // 찾은 결과
                }
            }

            // 찾은 결과를 classificationCode로 정렬
            val sortedProducts = products.sortedBy { it.classificationCode }

            // 결과 리턴
            if (sortedProducts.isNotEmpty()) {
                Log.d("FirestoreProductModel", "productId=1인 기자재를 리턴")
                callback(StatusCode.SUCCESS, sortedProducts)
            }
            else {
                Log.w("FirestoreProductModel", "productId=1인 기자재 리스트가 비어있음")
                callback(StatusCode.FAILURE, null)
            }
        }.addOnFailureListener { e ->
            Log.w("FirestoreProductModel", "productId=1인 기자재를 firestore에서 불러오기 실패", e)
            callback(StatusCode.FAILURE, null)
        }
    }

    /**
     * Firestore에서 특정 classificicationCode의 기자재 정보를 조회합니다.
     * RecyclerView에서 classificationCode에 해당하는 기자재 리스트를 띄우기 위함
     *
     * @param classficationCode 정보를 조회할 기자재의 종류 코드입니다.
     * @param callback 기자재 정보 조회 상태 코드(STATUS_CODE)와 기자재 리스트를 반환하는 콜백 함수입니다.
     */
    fun getProductsByClassificationCode(classificationCode: Int, callback: (Int, List<Product>?) -> Unit) {
        val productsRef = db.collection("Product")
        val borrowingsRef = db.collection("Borrowing")

        val products = mutableListOf<Product>()
        val tasks = mutableListOf<Task<QuerySnapshot>>()

        // classificationCode에 해당하는 기자재를 Product, Borrowing 컬렉션에서 찾음
        val productsTask = productsRef.whereEqualTo("classificationCode", classificationCode).get()
        tasks.add(productsTask)
        val borrowingsTask = borrowingsRef.whereEqualTo("classificationCode", classificationCode).get()
        tasks.add(borrowingsTask)

        Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnSuccessListener { results ->
            Log.d("FirestoreProductModel", "classificationCode에 해당하는 기자재를 firestore에서 불러오기 성공")

            // Product 컬렉션에서 찾은 결과를 리스트에 넣음
            val productsDocuments = results[0]
            for (document in productsDocuments) {
                val product = document.toObject<Product>().apply {
                    canBorrow = true
                    documentId = document.id
                }
                products.add(product)
            }

            // Borrowing 컬렉션에서 찾은 결과를 리스트에 넣음
            val borrowingsDocuments = results[1]
            for (document in borrowingsDocuments) {
                val product = document.toObject<Product>().apply {
                    canBorrow = false
                    documentId = document.id
                }
                products.add(product)
            }

            // 찾은 결과를 productId로 정렬
            val sortedProducts = products.sortedBy { it.productId }

            // 결과 리턴
            if (sortedProducts.isNotEmpty()) {
                Log.d("FirestoreProductModel", "classificationCode에 해당하는 기자재를 리턴")
                callback(StatusCode.SUCCESS, sortedProducts)
            }
            else {
                Log.w("FirestoreProductModel", "classificationCode에 해당하는 기자재 리스트가 비어있음")
                callback(StatusCode.FAILURE, null)
            }
        }.addOnFailureListener { e ->
            Log.w("FirestoreProductModel", "classificationCode에 해당하는 기자재를 firestore에서 불러오기 실패", e)
            callback(StatusCode.FAILURE, null)
        }
    }

    /**
     * Firestore에서 특정 ID의 기자재 상세 정보를 Product 또는 Borrowing 컬렉션에서 조회합니다.
     *
     * @param documentId 조회를 위한 기자재 document의 ID입니다.
     * @param canBorrow true면 Product 컬렉션에서, false면 Borrowing 컬렉션에서 조회합니다.
     * @param callback 기자재 정보 조회 상태 코드(STATUS_CODE)와 기자재를 반환하는 콜백 함수입니다.
     */
    fun getProductsByDocumentId(documentId: String, canBorrow: Boolean, callback: (Int, Product?) -> Unit) {
        val collectionRef = if (canBorrow) db.collection("Product") else db.collection("Borrowing")
        collectionRef.document(documentId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val product = documentSnapshot.toObject<Product>()?.apply {
                        this.canBorrow = canBorrow
                        this.documentId = documentId
                    }
                    if (product != null) {
                        Log.d("FirestoreProductModel", "${product.name} 기자재 상세 정보 가져오기 성공")
                        callback(StatusCode.SUCCESS, product)
                    }
                    else {
                        Log.w("FirestoreProductModel", "$documentId 기자재 상세 정보가 존재하지 않음")
                        callback(StatusCode.FAILURE, null)
                    }
                }
                else {
                    Log.w("FirestoreProductModel", "$documentId 기자재 상세 정보 가져오기 실패")
                    callback(StatusCode.FAILURE, null)
                }
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreProductModel", "$documentId 기자재 상세 정보 가져오기 실패", e)
                callback(StatusCode.FAILURE, null)
            }
    }

    /**
     * Storage에서 이미지 파일 경로인 img 값을 읽어 Bitmap을 리턴합니다.
     *
     * @param imgUrl 비트맵으로 변환할 이미지 파일 경로입니다.
     * @param callback 상태 코드(STATUS_CODE)와 변환한 이미지 비트맵을 반환하는 콜백 함수입니다.
     */
    fun getProductImgBitmap(imgUrl: String, callback: (Int, Bitmap?) -> Unit) {
        val imgRef = Firebase.storage.getReferenceFromUrl(imgUrl)
        if (imgRef == null) {
            Log.w("FirestoreProductModel", "이미지 레퍼런스가 null")
            callback(StatusCode.FAILURE, null)
        }
        else {
            imgRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                if (bitmap == null) {
                    Log.w("FirestoreProductModel", "이미지 비트맵 변환 실패")
                    callback(StatusCode.FAILURE, null)
                }
                else {
                    Log.d("FirestoreProductModel", "이미지 비트맵 변환 성공")
                    callback(StatusCode.SUCCESS, bitmap)
                }
            }.addOnFailureListener {e ->
                Log.w("FirestoreProductModel", "이미지 다운로드 실패", e)
                callback(StatusCode.FAILURE, null)
            }
        }
    }

}