package com.example.autoreboot

import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val dayNames = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val dayBoxes = mutableListOf<CheckBox>()

    private lateinit var tvStatus: TextView
    private lateinit var tvNext: TextView
    private lateinit var swEnable: Switch
    private lateinit var timePicker: TimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvNext = findViewById(R.id.tvNext)
        swEnable = findViewById(R.id.swEnable)
        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)

        val container = findViewById<LinearLayout>(R.id.daysContainer)
        dayNames.forEach { name ->
            val cb = CheckBox(this).apply { text = name }
            dayBoxes.add(cb)
            container.addView(cb)
        }

        loadPrefs()

        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        findViewById<Button>(R.id.btnTest).setOnClickListener { confirmRebootNow() }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        updateNext(Scheduler.schedule(this))
    }

    private fun isDeviceOwner(): Boolean =
        getSystemService(DevicePolicyManager::class.java).isDeviceOwnerApp(packageName)

    private fun updateStatus() {
        tvStatus.text = if (isDeviceOwner()) {
            "Status: Device Owner AKTIF. Restart otomatis bisa berjalan."
        } else {
            "Status: BELUM jadi Device Owner, restart tidak akan berjalan.\n\n" +
                "Jalankan dari komputer (USB debugging aktif):\n" +
                "adb shell dpm set-device-owner com.example.autoreboot/.AdminReceiver"
        }
    }

    private fun loadPrefs() {
        val p = Scheduler.prefs(this)
        swEnable.isChecked = p.getBoolean(Scheduler.KEY_ENABLED, false)
        timePicker.hour = p.getInt(Scheduler.KEY_HOUR, 3)
        timePicker.minute = p.getInt(Scheduler.KEY_MINUTE, 0)
        val mask = p.getInt(Scheduler.KEY_DAYS, 0x7F)
        dayBoxes.forEachIndexed { i, cb -> cb.isChecked = ((mask shr i) and 1) == 1 }
    }

    private fun save() {
        var mask = 0
        dayBoxes.forEachIndexed { i, cb -> if (cb.isChecked) mask = mask or (1 shl i) }

        if (swEnable.isChecked && mask == 0) {
            Toast.makeText(this, "Pilih minimal satu hari", Toast.LENGTH_SHORT).show()
            return
        }

        Scheduler.prefs(this).edit()
            .putBoolean(Scheduler.KEY_ENABLED, swEnable.isChecked)
            .putInt(Scheduler.KEY_HOUR, timePicker.hour)
            .putInt(Scheduler.KEY_MINUTE, timePicker.minute)
            .putInt(Scheduler.KEY_DAYS, mask)
            .apply()

        updateNext(Scheduler.schedule(this))
        Toast.makeText(this, "Jadwal disimpan", Toast.LENGTH_SHORT).show()
    }

    private fun updateNext(t: Long?) {
        tvNext.text = if (t == null) {
            "Restart berikutnya: tidak dijadwalkan"
        } else {
            val f = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale("id"))
            "Restart berikutnya: " + f.format(Date(t))
        }
    }

    private fun confirmRebootNow() {
        if (!isDeviceOwner()) {
            Toast.makeText(this, "Belum jadi Device Owner", Toast.LENGTH_LONG).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Restart sekarang?")
            .setMessage("HP akan langsung restart.")
            .setPositiveButton("Restart") { _, _ ->
                val dpm = getSystemService(DevicePolicyManager::class.java)
                dpm.reboot(ComponentName(this, AdminReceiver::class.java))
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
