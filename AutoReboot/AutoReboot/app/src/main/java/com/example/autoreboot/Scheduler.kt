package com.example.autoreboot

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import java.util.Calendar

object Scheduler {

    private const val PREFS = "schedule_prefs"
    const val KEY_ENABLED = "enabled"
    const val KEY_HOUR = "hour"
    const val KEY_MINUTE = "minute"
    const val KEY_DAYS = "days" // bitmask: bit0 = Minggu ... bit6 = Sabtu

    // Disimpan di device-protected storage agar terbaca saat boot sebelum unlock
    fun prefs(ctx: Context): SharedPreferences =
        ctx.createDeviceProtectedStorageContext()
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun nextTrigger(hour: Int, minute: Int, mask: Int, now: Long = System.currentTimeMillis()): Long? {
        if (mask == 0) return null
        val cal = Calendar.getInstance()
        for (offset in 0..7) {
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dow = cal.get(Calendar.DAY_OF_WEEK) // 1 = Minggu ... 7 = Sabtu
            val dayOn = ((mask shr (dow - 1)) and 1) == 1
            if (cal.timeInMillis > now && dayOn) return cal.timeInMillis
        }
        return null
    }

    private fun pendingIntent(ctx: Context): PendingIntent {
        val intent = Intent(ctx, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        am.cancel(pendingIntent(ctx))
    }

    /** Pasang alarm berikutnya sesuai pengaturan. Mengembalikan waktu alarm, atau null jika nonaktif. */
    fun schedule(ctx: Context): Long? {
        cancel(ctx)
        val p = prefs(ctx)
        if (!p.getBoolean(KEY_ENABLED, false)) return null
        val t = nextTrigger(
            p.getInt(KEY_HOUR, 3),
            p.getInt(KEY_MINUTE, 0),
            p.getInt(KEY_DAYS, 0x7F)
        ) ?: return null

        val am = ctx.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(ctx)
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi)
        }
        return t
    }
}
