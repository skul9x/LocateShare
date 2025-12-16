package com.skul9x.locateshare

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.LocationData
import com.skul9x.locateshare.network.RetrofitClient
import com.skul9x.locateshare.network.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhoneActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEND == intent.action && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                // Extract URL using Regex
                val urlRegex = "(https?://\\S+)".toRegex()
                val matchResult = urlRegex.find(sharedText)
                val extractedUrl = matchResult?.value
                
                if (extractedUrl != null) {
                    // Show loading
                    statusText.text = "Đang gửi địa điểm..."
                    progressBar.visibility = View.VISIBLE
                    btnBack.visibility = View.GONE
                    
                    // Send directly without fetching name
                    sendLocationToServer(extractedUrl, "")
                } else {
                    showError("Không tìm thấy link Google Maps hợp lệ")
                }
            } else {
                showError("Không tìm thấy nội dung chia sẻ")
            }
        } else {
            // Not started via share, just show UI
            statusText.text = "Sẵn sàng gửi địa điểm..."
            progressBar.visibility = View.GONE
            btnBack.visibility = View.VISIBLE
        }
    }

    private fun sendLocationToServer(url: String, name: String) {
        val prefs = getSharedPreferences("LocateSharePrefs", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "")

        if (serverUrl.isNullOrEmpty()) {
            showError("Vui lòng cấu hình URL Server trong màn hình chính trước!")
            return
        }

        // Check if we have the cookie, if not, verify first
        if (com.skul9x.locateshare.network.RetrofitClient.cookie.isEmpty()) {
            com.skul9x.locateshare.network.HostingVerifier.verify(this, serverUrl) { success ->
                if (success) {
                    // Retry sending
                    performSend(serverUrl, url, name)
                } else {
                    showError("Không thể xác thực hosting. Vui lòng thử lại.")
                }
            }
        } else {
            performSend(serverUrl, url, name)
        }
    }

    private fun performSend(serverUrl: String, url: String, name: String) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getClient(serverUrl).create(ApiService::class.java)
                val responseBody = withContext(Dispatchers.IO) {
                    apiService.sendLocation(url, name)
                }

                val responseString = responseBody.string()
                AppLogger.log("Server Response: $responseString")

                // Check for hosting challenge (AES/Cookie)
                if (responseString.contains("__test") || responseString.contains("slowAES")) {
                    AppLogger.log("Cookie hết hạn hoặc chưa xác thực. Đang xác thực lại...")
                    com.skul9x.locateshare.network.RetrofitClient.cookie = ""
                    com.skul9x.locateshare.network.HostingVerifier.verify(this@PhoneActivity, serverUrl) { success ->
                        if (success) {
                            performSend(serverUrl, url, name)
                        } else {
                            showError("Xác thực thất bại. Vui lòng thử lại.")
                        }
                    }
                    return@launch
                }

                // Try to parse manually
                if (responseString.contains("success")) {
                    AppLogger.log("Gửi thành công: $url")
                    showSuccess()
                } else {
                    val msg = "Lỗi Server (Xem log để biết chi tiết)"
                    showError(msg)
                }
            } catch (e: Exception) {
                val msg = "Lỗi kết nối: ${e.message}"
                AppLogger.log(msg)
                showError(msg)
            }
        }
    }

    private fun showSuccess() {
        statusText.text = "Đã gửi thành công!"
        statusText.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
        progressBar.visibility = View.GONE
        btnBack.visibility = View.VISIBLE
        Toast.makeText(this, "Đã gửi đến xe!", Toast.LENGTH_LONG).show()
        
        // Optional: Close automatically after delay
        // finish()
    }

    private fun showError(message: String) {
        statusText.text = message
        statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
        progressBar.visibility = View.GONE
        btnBack.visibility = View.VISIBLE
    }
}
