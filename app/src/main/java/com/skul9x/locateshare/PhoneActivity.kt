package com.skul9x.locateshare

import android.content.Intent
import com.skul9x.locateshare.util.LocationParser
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skul9x.locateshare.network.AppLogger
import com.skul9x.locateshare.network.RetrofitClient
import com.skul9x.locateshare.network.UpdateCurrentLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhoneActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: Button

    private val api by lazy { RetrofitClient.getApiService() }

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
                    statusText.text = "Đang xử lý địa điểm..."
                    progressBar.visibility = View.VISIBLE
                    btnBack.visibility = View.GONE

                    lifecycleScope.launch {
                        var extractedName = LocationParser.parseNameFromSharedText(sharedText, extractedUrl)
                        if (extractedName.isEmpty() && extractedUrl.contains("maps.app.goo.gl")) {
                            statusText.text = "Đang giải mã URL..."
                            try {
                                extractedName = LocationParser.resolveRedirectUrlToName(extractedUrl)
                            } catch (e: Exception) {
                                AppLogger.log("Failed to resolve redirect: ${e.message}")
                            }
                        }
                        statusText.text = "Đang gửi địa điểm..."
                        sendLocationToSupabase(extractedUrl, extractedName)
                    }
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

    private fun sendLocationToSupabase(url: String, name: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.updateCurrentLocation(
                        body = UpdateCurrentLocation(url = url, name = name)
                    )
                }
                AppLogger.log("Gửi thành công: $url")
                showSuccess()
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
    }

    private fun showError(message: String) {
        statusText.text = message
        statusText.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
        progressBar.visibility = View.GONE
        btnBack.visibility = View.VISIBLE
    }
}
