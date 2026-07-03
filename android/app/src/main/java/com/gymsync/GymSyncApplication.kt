package com.gymsync

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.gymsync.util.NotificationHelper

@HiltAndroidApp
class GymSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
