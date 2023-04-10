package com.onlinemusic.wemu.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.onlinemusic.wemu.musicplayerdemo.ApplicationClass
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WemuApplication: Application() {
    companion object{
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }
    private lateinit var analytics: FirebaseAnalytics
    override fun onCreate() {
        super.onCreate()
        analytics = Firebase.analytics
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(
                ApplicationClass.CHANNEL_ID,"Now Playing",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is an important channel for showing song"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}