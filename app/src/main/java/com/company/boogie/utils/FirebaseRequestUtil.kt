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
     * Product컬렉션(대여가능기자재)에 있는 문서를 Borrowing컬렉션(대여중인 기자재)로 이동시킵니다
     */

    fun moveProductToBorrowing(productId: String) {
        val db = FirebaseFirestore.getInstance()

        // Product 컬렉션에서 문서 가져오기
        val productRef = db.collection("Product").document(productId)
        productRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data

                // Borrowing 컬렉션에 문서 추가
                val borrowingRef = db.collection("Product").document(productId)
                borrowingRef.set(data!!)
                    .addOnSuccessListener {
                        // 문서 추가 성공시, 원본 문서 삭제
                        productRef.delete().addOnSuccessListener {
                            println("Document successfully moved!")
                        }.addOnFailureListener { e ->
                            println("Error deleting document: $e")
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error adding document: $e")
                    }
            } else {
                println("No such document!")
            }
        }.addOnFailureListener { e ->
            println("Error getting document: $e")
        }
    }

    /**
     *  Borrowing컬렉션(대여중인 기자재)에 있는 문서를 Product컬렉션(대여가능기자재)로 이동시킵니다
     */
    fun moveProductToProduct(productId: String) {
        val db = FirebaseFirestore.getInstance()

        // Product 컬렉션에서 문서 가져오기
        val productRef = db.collection("Borrowing").document(productId)
        productRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data

                // Borrowing 컬렉션에 문서 추가
                val borrowingRef = db.collection("Borrowing").document(productId)
                borrowingRef.set(data!!)
                    .addOnSuccessListener {
                        // 문서 추가 성공시, 원본 문서 삭제
                        productRef.delete().addOnSuccessListener {
                            println("Document successfully moved!")
                        }.addOnFailureListener { e ->
                            println("Error deleting document: $e")
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error adding document: $e")
                    }
            } else {
                println("No such document!")
            }
        }.addOnFailureListener { e ->
            println("Error getting document: $e")
        }
    }


}