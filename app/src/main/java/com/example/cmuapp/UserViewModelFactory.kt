package com.example.cmuapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmuapp.data.database.AppDatabase
import com.example.cmuapp.viewmodel.UserViewModel

class UserViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}