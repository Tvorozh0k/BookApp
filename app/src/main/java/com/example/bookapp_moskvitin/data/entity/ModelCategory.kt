package com.example.bookapp_moskvitin.data.entity

class ModelCategory {

    // variables must match as in firebase
    var id:String = ""
    var timestamp:Long = 0
    var category:String = ""
    var uid:String = ""

    // empty constructor, required by Firebase
    constructor()

    // parameterized constructor
    constructor(id: String, timestamp: Long, category: String, uid: String) {
        this.id = id
        this.timestamp = timestamp
        this.category = category
        this.uid = uid
    }
}