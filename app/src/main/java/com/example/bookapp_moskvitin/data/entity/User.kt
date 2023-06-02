package com.example.bookapp_moskvitin.data.entity

// Users
data class User(
   val uid: String = "",
   val email: String = "",
   val name: String = "",
   val profileImage: String = "", // imageUrl
   val userType: String = "",
   val timestamp: Long = 0
)
