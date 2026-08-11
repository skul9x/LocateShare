package com.skul9x.locateshare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skul9x.locateshare.network.AppLogger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPhoneMode = findViewById<Button>(R.id.btnPhoneMode)
        val btnCarMode = findViewById<Button>(R.id.btnCarMode)

        val prefs = getSharedPreferences("LocateSharePrefs", Context.MODE_PRIVATE)

        // Auto-launch default mode
        val rgDefaultMode = findViewById<android.widget.RadioGroup>(R.id.rgDefaultMode)
        val defaultMode = prefs.getString("default_mode", "none")

        when (defaultMode) {
            "phone" -> {
                rgDefaultMode.check(R.id.rbPhone)
                startActivity(Intent(this, PhoneActivity::class.java))
            }
            "car" -> {
                rgDefaultMode.check(R.id.rbCar)
                startActivity(Intent(this, CarActivity::class.java))
            }
            else -> rgDefaultMode.check(R.id.rbNone)
        }

        rgDefaultMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbPhone -> "phone"
                R.id.rbCar -> "car"
                else -> "none"
            }
            prefs.edit().putString("default_mode", mode).apply()
        }

        btnPhoneMode.setOnClickListener {
            startActivity(Intent(this, PhoneActivity::class.java))
        }

        btnCarMode.setOnClickListener {
            startActivity(Intent(this, CarActivity::class.java))
        }

        val btnViewLogs = findViewById<Button>(R.id.btnViewLogs)
        btnViewLogs.setOnClickListener {
            val logs = AppLogger.getLogs()
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logs")
                .setMessage(if (logs.isNotEmpty()) logs else "Chưa có log nào.")
                .setPositiveButton("Copy") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("LocateShare Logs", logs)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Đã copy log!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Đóng", null)
                .create()
            dialog.show()
        }
    }
}