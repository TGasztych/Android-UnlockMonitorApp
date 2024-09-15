package com.example.unlockmonitorapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnlockAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateTime: String,
    val photoPath: String,
    val latitude: Double,
    val longitude: Double
)
