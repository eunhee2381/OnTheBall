package com.company.boogie.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("birthday") @set:PropertyName("birthday") var birthday: Timestamp = Timestamp.now(),
    @get:PropertyName("studentID") @set:PropertyName("studentID") var studentID: Int = 0,
    @get:PropertyName("isAdmin") @set:PropertyName("isAdmin") var isAdmin: Boolean = false,
    @get:PropertyName("isBanned") @set:PropertyName("isBanned") var isBanned: Boolean = false,
    @get:PropertyName("borrowing") @set:PropertyName("borrowing") var borrowing: String = "",
    @get:PropertyName("token") @set:PropertyName("token") var token: String = ""
)
