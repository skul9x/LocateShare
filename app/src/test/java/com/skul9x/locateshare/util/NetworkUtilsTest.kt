package com.skul9x.locateshare.util

import android.os.Build
import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkUtilsTest {

    @Test
    fun testIsConnectionFailureWithNetworkExceptions() {
        val dnsException = UnknownHostException("Unable to resolve host \"supabase.co\"")
        assertTrue("UnknownHostException should be classified as connection failure", NetworkUtils.isConnectionFailure(dnsException))

        val timeoutException = SocketTimeoutException("timeout")
        assertTrue("SocketTimeoutException should be classified as connection failure", NetworkUtils.isConnectionFailure(timeoutException))

        val connectException = ConnectException("Failed to connect")
        assertTrue("ConnectException should be classified as connection failure", NetworkUtils.isConnectionFailure(connectException))

        val genericIoException = IOException("Network stream closed")
        assertTrue("General IOException should be classified as connection failure", NetworkUtils.isConnectionFailure(genericIoException))
    }

    @Test
    fun testIsConnectionFailureWithWrappedNetworkExceptions() {
        val wrappedException = RuntimeException("Wrapper exception", UnknownHostException("Unable to resolve host \"supabase.co\""))
        assertTrue("Nested UnknownHostException should be identified as connection failure", NetworkUtils.isConnectionFailure(wrappedException))
    }

    @Test
    fun testIsConnectionFailureWithNonNetworkExceptions() {
        val illegalStateException = IllegalStateException("Something is in illegal state")
        assertFalse("IllegalStateException must not be classified as connection failure", NetworkUtils.isConnectionFailure(illegalStateException))

        val nullPointerException = NullPointerException("Object reference is null")
        assertFalse("NullPointerException must not be classified as connection failure", NetworkUtils.isConnectionFailure(nullPointerException))

        val illegalArgumentException = IllegalArgumentException("Invalid argument")
        assertFalse("IllegalArgumentException must not be classified as connection failure", NetworkUtils.isConnectionFailure(illegalArgumentException))

        assertFalse("null should evaluate to false", NetworkUtils.isConnectionFailure(null))
    }

    @Test
    fun testWifiSettingsActionAcrossApiVersions() {
        val modernAction = NetworkUtils.getWifiSettingsAction(Build.VERSION_CODES.Q)
        assertEquals("Android 10+ (API 29+) should use Settings.Panel.ACTION_INTERNET_CONNECTIVITY",
            Settings.Panel.ACTION_INTERNET_CONNECTIVITY, modernAction)

        val legacyAction = NetworkUtils.getWifiSettingsAction(Build.VERSION_CODES.P)
        assertEquals("Android 9 and below should use Settings.ACTION_WIFI_SETTINGS",
            Settings.ACTION_WIFI_SETTINGS, legacyAction)
    }

    @Test
    fun testWifiSettingsIntentGeneration() {
        val intentModern = NetworkUtils.getWifiSettingsIntent(sdkInt = Build.VERSION_CODES.Q)
        assertNotNull("Intent for Android 10+ should not be null", intentModern)

        val intentLegacy = NetworkUtils.getWifiSettingsIntent(sdkInt = Build.VERSION_CODES.P)
        assertNotNull("Intent for legacy Android should not be null", intentLegacy)
    }
}
