package com.gymsync.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gymsync.R

object NotificationHelper {
    const val CHANNEL_PARTNER = "partner_status"
    const val CHANNEL_WATER = "water_reminder"
    const val CHANNEL_WORKOUT = "workout_updates"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val partnerChannel = NotificationChannel(
            CHANNEL_PARTNER,
            "Partner Status",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when your partner comes online or changes status"
        }

        val waterChannel = NotificationChannel(
            CHANNEL_WATER,
            "Water Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to drink water during workouts"
        }

        val workoutChannel = NotificationChannel(
            CHANNEL_WORKOUT,
            "Workout Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Updates from your partner's workout"
        }

        manager.createNotificationChannel(partnerChannel)
        manager.createNotificationChannel(waterChannel)
        manager.createNotificationChannel(workoutChannel)
    }

    fun showPartnerOnlineNotification(context: Context, partnerName: String) {
        if (!hasPermission(context)) return

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PARTNER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$partnerName is online")
            .setContentText("Your workout partner is now available!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }

    fun showPartnerWorkoutNotification(context: Context, partnerName: String) {
        if (!hasPermission(context)) return

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$partnerName started a workout!")
            .setContentText("Join them or cheer them on!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1002, notification)
    }

    private fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}
