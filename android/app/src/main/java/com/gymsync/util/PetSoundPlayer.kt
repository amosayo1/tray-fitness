package com.gymsync.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import java.io.File

object PetSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playBark(context: Context) {
        playRaw(context, "dog_bark")
    }

    fun playMeow(context: Context) {
        playRaw(context, "cat_meow")
    }

    fun playNotification(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (notification != null) {
                playUri(context, notification)
            }
        } catch (_: Exception) { }
    }

    private fun playRaw(context: Context, name: String) {
        try {
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) {
                release()
                mediaPlayer = MediaPlayer.create(context, resId).apply {
                    setOnCompletionListener { release() }
                    start()
                }
            } else {
                playNotification(context)
            }
        } catch (_: Exception) {
            playNotification(context)
        }
    }

    private fun playUri(context: Context, uri: Uri) {
        try {
            release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                setOnCompletionListener { release() }
                start()
            }
        } catch (_: Exception) { }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
