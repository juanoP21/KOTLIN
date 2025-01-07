package com.tati

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var daysValue: TextView
    private lateinit var hoursValue: TextView
    private lateinit var minutesValue: TextView
    private lateinit var secondsValue: TextView

    private var countdownService: CountdownService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as CountdownService.CountdownBinder
            countdownService = serviceBinder.getService()
            isBound = true
            Log.d("MainActivity", "Servicio conectado. Iniciando actualización de cuenta regresiva.")
            updateCountdown()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w("MainActivity", "Servicio desconectado.")
            countdownService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular los TextViews
        daysValue = findViewById(R.id.days_value)
        hoursValue = findViewById(R.id.hours_value)
        minutesValue = findViewById(R.id.minutes_value)
        secondsValue = findViewById(R.id.seconds_value)

        Log.d("MainActivity", "Inicializando la actividad y vinculando servicio.")

        // Iniciar y vincular el servicio
        Intent(this, CountdownService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun updateCountdown() {
        val handler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                countdownService?.let { service ->
                    val remainingTime = service.remainingTime
                    Log.d("MainActivity", "Tiempo restante recibido: $remainingTime ms")
                    updateCountdownUI(remainingTime)
                    if (remainingTime > 0) {
                        handler.postDelayed(this, 1000) // Reprogramar cada segundo
                    } else {
                        Log.i("MainActivity", "Cuenta regresiva completada.")
                    }
                } ?: Log.w("MainActivity", "Servicio no disponible.")
            }
        }
        handler.post(runnable)
    }

    private fun updateCountdownUI(timeInMillis: Long) {
        val days = (timeInMillis / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((timeInMillis / (1000 * 60 * 60)) % 24).toInt()
        val minutes = ((timeInMillis / (1000 * 60)) % 60).toInt()
        val seconds = ((timeInMillis / 1000) % 60).toInt()

        Log.d("MainActivity", "Actualizando UI: Días=$days, Horas=$hours, Minutos=$minutes, Segundos=$seconds")

        daysValue.text = days.toString().padStart(2, '0')
        hoursValue.text = hours.toString().padStart(2, '0')
        minutesValue.text = minutes.toString().padStart(2, '0')
        secondsValue.text = seconds.toString().padStart(2, '0')
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            Log.d("MainActivity", "Desvinculando el servicio.")
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
