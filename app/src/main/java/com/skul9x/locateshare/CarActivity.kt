package com.skul9x.locateshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skul9x.locateshare.network.ApiService
import com.skul9x.locateshare.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CarActivity : AppCompatActivity() {

    private lateinit var tvLocation: TextView
    private lateinit var btnOpenMap: Button
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentUrl: String = ""
    private var isPolling = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        tvLocation = findViewById(R.id.tvLocation)
        btnOpenMap = findViewById(R.id.btnOpenMap)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnFavorites = findViewById<Button>(R.id.btnFavorites)

        btnBack.setOnClickListener {
            finish()
        }

        btnReload.setOnClickListener {
            val prefs = getSharedPreferences("LocateSharePrefs", Context.MODE_PRIVATE)
            val serverUrl = prefs.getString("server_url", "") ?: ""
            if (serverUrl.isNotEmpty()) {
                verifyHosting(serverUrl)
            } else {
                Toast.makeText(this, "Chưa cấu hình URL!", Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenMap.setOnClickListener {
            if (currentUrl.isNotEmpty()) {
                openMap(currentUrl)
            } else {
                Toast.makeText(this, "Chưa có địa điểm nào!", Toast.LENGTH_SHORT).show()
            }
        }

        btnFavorites.setOnClickListener {
            openMap("https://goo.gl/maps/GvWoy8V6Dn8hRTM67")
        }

        startPolling()
    }

    private fun startPolling() {
        val prefs = getSharedPreferences("LocateSharePrefs", Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "")

        if (serverUrl.isNullOrEmpty()) {
            tvLocation.text = "Vui lòng cấu hình URL Server!"
            return
        }

        val pollRunnable = object : Runnable {
            override fun run() {
                if (!isPolling) return
                fetchLocation(serverUrl)
                handler.postDelayed(this, 3000) // Poll every 3 seconds
            }
        }
        handler.post(pollRunnable)
    }

    private fun fetchLocation(baseUrl: String) {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getClient(baseUrl).create(ApiService::class.java)
                val responseBody = withContext(Dispatchers.IO) {
                    apiService.getLocation()
                }
                
                val responseString = responseBody.string()
                // AppLogger.log("Polling Response: $responseString") // Uncomment if needed, but spammy

                // Simple manual parse
                if (responseString.contains("\"url\":\"")) {
                    var extractedUrl = responseString.substringAfter("\"url\":\"").substringBefore("\"")
                    
                    // Fix JSON escaping (https:\/\/ -> https://)
                    extractedUrl = extractedUrl.replace("\\/", "/")
                    
                    if (extractedUrl.isNotEmpty() && extractedUrl != currentUrl) {
                        currentUrl = extractedUrl
                        updateUI(currentUrl)
                    }
                }
            } catch (e: Exception) {
                // Silent fail on polling error
            }
        }
    }

    private fun updateUI(url: String) {
        tvLocation.text = url
        tvLocation.setTextColor(android.graphics.Color.WHITE)
    }

    private fun openMap(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser if Maps app not installed
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyHosting(url: String) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_webview)
        val webView = dialog.findViewById<android.webkit.WebView>(R.id.webView)
        
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookies = android.webkit.CookieManager.getInstance().getCookie(url)
                if (cookies != null && cookies.contains("__test")) {
                    com.skul9x.locateshare.network.RetrofitClient.cookie = cookies
                    Toast.makeText(this@CarActivity, "Đã xác thực! Đang tải lại...", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    // Reload data immediately
                    val prefs = getSharedPreferences("LocateSharePrefs", Context.MODE_PRIVATE)
                    val serverUrl = prefs.getString("server_url", "") ?: ""
                    if (serverUrl.isNotEmpty()) {
                        fetchLocation(serverUrl)
                    }
                }
            }
        }
        
        webView.loadUrl(url)
        dialog.show()
        Toast.makeText(this, "Đang xác thực...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
        handler.removeCallbacksAndMessages(null)
    }
}
