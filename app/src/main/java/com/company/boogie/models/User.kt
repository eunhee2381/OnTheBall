package com.company.boogie.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("email") val email: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("birthday") val birthday: Timestamp = Timestamp.now(),
    @PropertyName("studentID") val studentID: Int = 0,
    @get:PropertyName("isAdmin") @set:PropertyName("isAdmin") var isAdmin: Boolean = false,
    @get:PropertyName("isBanned") @set:PropertyName("isBanned") var isBanned: Boolean = false,
    @get:PropertyName("borrowing") @set:PropertyName("borrowing") var borrowing: String = "",
    @get:PropertyName("token") @set:PropertyName("token") var token: String = ""
)
