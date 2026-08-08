package com.skul9x.locateshare.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {

    /**
     * Checks if the device has an active validated internet connection using modern NetworkCapabilities.
     */
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Checks if the active network connection is Wi-Fi.
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Classifies whether a Throwable represents a network/connection failure
     * (e.g. UnknownHostException, SocketTimeoutException, ConnectException, or general IOException).
     */
    fun isConnectionFailure(throwable: Throwable?): Boolean {
        var current = throwable
        while (current != null) {
            if (current is UnknownHostException ||
                current is SocketTimeoutException ||
                current is ConnectException ||
                current is IOException
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    /**
     * Resolves the appropriate Wi-Fi/Internet settings intent action based on API level.
     * Uses Settings.Panel.ACTION_INTERNET_CONNECTIVITY for Android 10+ (API 29+),
     * and Settings.ACTION_WIFI_SETTINGS for older Android versions.
     */
    fun getWifiSettingsAction(sdkInt: Int = Build.VERSION.SDK_INT): String {
        return if (sdkInt >= Build.VERSION_CODES.Q) {
            Settings.Panel.ACTION_INTERNET_CONNECTIVITY
        } else {
            Settings.ACTION_WIFI_SETTINGS
        }
    }

    /**
     * Returns the appropriate Wi-Fi/Internet settings intent based on Android version.
     */
    fun getWifiSettingsIntent(context: Context? = null, sdkInt: Int = Build.VERSION.SDK_INT): Intent {
        val action = getWifiSettingsAction(sdkInt)
        val intent = Intent(action)
        if (context != null && context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return intent
    }

    /**
     * Safely attempts to open the Wi-Fi settings or Internet connectivity panel,
     * handling ActivityNotFoundException and returning true on success or false on failure.
     */
    fun openWifiSettings(context: Context): Boolean {
        return try {
            val intent = getWifiSettingsIntent(context)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                if (context !is Activity) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
