package com.company.boogie.models

import com.google.firebase.firestore.Exclude


data class Product(
    val name: String = "",
    val img: String = "",
    val detail: String = "",
    val location: String = "",
    val classificationCode: Int = -1,
    val productId: Int = 0,
    @Exclude var canBorrow: Boolean = false,
    @Exclude var documentId: String = ""
)