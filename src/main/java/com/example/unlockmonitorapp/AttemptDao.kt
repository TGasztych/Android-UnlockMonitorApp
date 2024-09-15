package com.example.unlockmonitorapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttemptDao {
    @Insert
    fun insertAttempt(attempt: UnlockAttempt)

    @Query("SELECT * FROM UnlockAttempt ORDER BY id DESC")
    fun getAllAttempts(): LiveData<List<UnlockAttempt>>
}
