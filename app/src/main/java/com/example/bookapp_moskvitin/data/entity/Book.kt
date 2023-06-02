package com.example.bookapp_moskvitin.data.entity

// Books
data class Book(
    val id: String = "",
    val uid: String = "",
    val categoryId: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val timestamp: Long = 0,
    val viewsCount: Int = 0,
    val downloadsCount: Int = 0
)