package com.example.unlockmonitorapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class UnlockViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getDatabase(application)
    val attempts: LiveData<List<UnlockAttempt>> = db.attemptDao().getAllAttempts()
}

