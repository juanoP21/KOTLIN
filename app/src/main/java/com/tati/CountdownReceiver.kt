package com.tati

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CountdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val remainingTime = intent?.getLongExtra("remainingTime", 0) ?: 0
        Log.d("CountdownReceiver", "Tiempo recibido en receptor: $remainingTime ms")
    }
}
