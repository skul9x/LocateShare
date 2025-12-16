package com.skul9x.locateshare.network

import android.app.Dialog
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.skul9x.locateshare.R

object HostingVerifier {

    fun verify(context: Context, url: String, onResult: (Boolean) -> Unit) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_webview)
        val webView = dialog.findViewById<WebView>(R.id.webView)

        if (webView == null) {
            Toast.makeText(context, "Lỗi: Không tìm thấy WebView layout!", Toast.LENGTH_SHORT).show()
            onResult(false)
            return
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val cookies = CookieManager.getInstance().getCookie(url)
                if (cookies != null && cookies.contains("__test")) {
                    RetrofitClient.cookie = cookies
                    Toast.makeText(context, "Đã xác thực thành công!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    onResult(true)
                }
            }
        }

        webView.loadUrl(url)
        dialog.show()
        Toast.makeText(context, "Đang xác thực hosting... Vui lòng đợi.", Toast.LENGTH_LONG).show()
    }
}
