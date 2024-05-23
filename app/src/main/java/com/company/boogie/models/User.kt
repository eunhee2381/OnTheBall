package com.company.boogie.models

import com.google.firebase.Timestamp
import java.util.Date

data class User(
    val email: String = "",
    val name: String = "",
    val birthday: Timestamp = Timestamp(Date(0, 0, 1)),
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val borrowing: String = "",
    val token: String = ""
)
