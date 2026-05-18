package com.togalugombe.aiguide.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Timestamp? = null
)
