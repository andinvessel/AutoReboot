package com.example.autoreboot

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Pasang alarm berikutnya dulu, baru restart
        Scheduler.schedule(context)

        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            dpm.reboot(ComponentName(context, AdminReceiver::class.java))
        }
    }
}
