package com.togalugombe.aiguide.data.model

import com.google.firebase.Timestamp

data class HistoryPost(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: Timestamp? = null
)
