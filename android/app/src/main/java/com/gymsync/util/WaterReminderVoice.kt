package com.gymsync.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object WaterReminderVoice {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
            }
        }
    }

    fun speakWaterReminder(petName: String, petType: Int) {
        val message = when (petType) {
            0 -> "$petName says woof! Time to drink water!"
            else -> "$petName says meow! Time to drink water!"
        }
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speak(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
