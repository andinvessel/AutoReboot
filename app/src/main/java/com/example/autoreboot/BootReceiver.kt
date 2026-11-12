package com.example.autoreboot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Alarm hilang setelah restart, jadi pasang ulang
        Scheduler.schedule(context)
    }
}
