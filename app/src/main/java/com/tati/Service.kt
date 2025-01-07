package com.tati

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class CountdownService : Service() {

    private val binder = CountdownBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val eventDate = "2024-12-25T00:00:00"
    private val eventDateTime =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Bogota")
        }.parse(eventDate)?.time ?: 0
    var remainingTime: Long = 0
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("CountdownService", "Servicio creado")
        createNotificationChannel()
        startForeground(1, createNotification("Cuenta regresiva iniciada..."))
        Log.d("CountdownService", "Notificación inicial creada")

        // Iniciar la cuenta regresiva al crear el servicio
        startCountdown()
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("CountdownService", "Servicio vinculado")
        return binder
    }

    inner class CountdownBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
    }

    private fun startCountdown() {
        Log.d("CountdownService", "Inicio de la cuenta regresiva")
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                remainingTime = eventDateTime - currentTime

                if (remainingTime > 0) {
                    Log.d("CountdownService", "Tiempo restante: $remainingTime ms")
                    updateNotification(remainingTime)
                    handler.postDelayed(this, 1000) // Reprogramar cada segundo
                } else {
                    Log.w("CountdownService", "Tiempo restante es 0. Finalizando cuenta regresiva")
                    remainingTime = 0
                    updateNotification(0)
                    switchToNewScreen() // Cambiar de pantalla cuando llega a 0
                    stopSelf() // Detener el servicio
                }
            }
        }
        handler.post(runnable)
    }

    private fun createNotification(content: String): android.app.Notification {
        val channelId = "countdown_channel"
        Log.d("CountdownService", "Creando notificación con contenido: $content")
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Cuenta regresiva")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "countdown_channel"
            val channelName = "Countdown Service"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d("CountdownService", "Canal de notificación creado: $channelName")
        }
    }

    private fun updateNotification(remainingTime: Long) {
        val days = (remainingTime / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((remainingTime / (1000 * 60 * 60)) % 24).toInt()
        val minutes = ((remainingTime / (1000 * 60)) % 60).toInt()
        val seconds = ((remainingTime / 1000) % 60).toInt()

        val timeText = "Días: ${days.toString().padStart(2, '0')} " +
                "Horas: ${hours.toString().padStart(2, '0')} " +
                "Min: ${minutes.toString().padStart(2, '0')} " +
                "Seg: ${seconds.toString().padStart(2, '0')}"

        Log.d("CountdownService", "Actualizando notificación con tiempo: $timeText")

        val notification = createNotification(timeText)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun switchToNewScreen() {
        Log.d("CountdownService", "Cambiando a la pantalla: Carta")
        val intent = Intent(this, Carta::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
