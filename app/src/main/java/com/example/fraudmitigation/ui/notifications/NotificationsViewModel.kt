package com.example.fraudmitigation.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to Fraud Mitigation notification service"
    }
    val text: LiveData<String> = _text
}