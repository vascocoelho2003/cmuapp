package com.example.cmuapp.data.entities

import androidx.room.Entity

@Entity(tableName = "user_visited", primaryKeys = ["userId", "establishmentId"])
data class UserVisitedEntity(
    val userId: String,
    val establishmentId: String
)