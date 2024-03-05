package com.example.cloudnote.services

import com.google.firebase.messaging.FirebaseMessagingService

class AppFCMService : FirebaseMessagingService()
{
    override fun onNewToken(token: String)
    {
        val prefsEditor = applicationContext.getSharedPreferences("my_prefs", MODE_PRIVATE).edit()
        prefsEditor.putString("fcmToken", token)
        prefsEditor.apply()
    }
}