package com.gymsync.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gymsync.R
import com.gymsync.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    companion object {
        var currentSteps: Int = 0
            private set
        var isTracking: Boolean = false
            private set

        private val _stepCount = MutableStateFlow(0)
        val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

        private var baselineSteps: Float = 0f

        fun start(context: Context) {
            val intent = Intent(context, StepCounterService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StepCounterService::class.java))
        }
    }

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var scope: CoroutineScope? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_STEP_COUNTER)
            .setContentTitle("GymSync")
            .setContentText("Tracking your steps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .build()

        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isTracking = true
        val prefs = getSharedPreferences("step_counter", MODE_PRIVATE)
        baselineSteps = prefs.getFloat("baseline", -1f)

        stepSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values == null) return
        val totalSteps = event.values[0]
        val prefs = getSharedPreferences("step_counter", MODE_PRIVATE)

        if (baselineSteps < 0) {
            baselineSteps = totalSteps
            prefs.edit().putFloat("baseline", baselineSteps).apply()
        }

        val steps = (totalSteps - baselineSteps).toInt().coerceAtLeast(0)
        currentSteps = steps
        _stepCount.value = steps
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isTracking = false
        sensorManager.unregisterListener(this)
        scope?.cancel()
    }
}
