package com.skul9x.locateshare

import com.skul9x.locateshare.util.SupabaseConnectionGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CarActivitySupabaseConnectionFailureTest {

    private lateinit var guard: SupabaseConnectionGuard

    @Before
    fun setUp() {
        guard = SupabaseConnectionGuard()
    }

    @Test
    fun testFirstConnectionFailureTriggersWifiAndSetsGuardFlag() {
        assertFalse("Guard flag should initially be false", guard.hasAutoOpenedWifiOnFailure)

        val dnsException = UnknownHostException("Unable to resolve host \"supabase.co\"")
        val action = guard.handleFetchError(dnsException, isManualReload = false)

        assertTrue("First connection failure must trigger Wi-Fi settings auto-open", action.shouldOpenWifi)
        assertTrue("Guard flag must be set to true to prevent loops", guard.hasAutoOpenedWifiOnFailure)
        assertEquals(
            "Location text should notify user of network/supabase failure",
            "⚠️ Không có kết nối mạng (Lỗi kết nối Supabase)",
            action.locationMessage
        )
        assertEquals(
            "Toast should inform driver that Wi-Fi settings are opening",
            "Không thể kết nối đến máy chủ Supabase. Đang mở cài đặt Wi-Fi...",
            action.toastMessage
        )
    }

    @Test
    fun testSubsequentConnectionFailureThrottlesAutoOpenToPreventInfiniteLoop() {
        // First failure triggers Wi-Fi
        val firstException = SocketTimeoutException("Read timeout")
        val firstAction = guard.handleFetchError(firstException, isManualReload = false)
        assertTrue(firstAction.shouldOpenWifi)
        assertTrue(guard.hasAutoOpenedWifiOnFailure)

        // Subsequent failure (e.g. after returning to app from settings without Wi-Fi)
        val secondException = ConnectException("Failed to connect to supabase.co")
        val secondAction = guard.handleFetchError(secondException, isManualReload = false)

        assertFalse("Subsequent failure must NOT re-trigger Wi-Fi auto-open (infinite loop prevention)", secondAction.shouldOpenWifi)
        assertTrue("Guard flag should remain true", guard.hasAutoOpenedWifiOnFailure)
        assertEquals(
            "Location text should still indicate no network connection",
            "⚠️ Không có kết nối mạng (Lỗi kết nối Supabase)",
            secondAction.locationMessage
        )
        assertTrue(
            "Toast message should inform about server connection error without launching settings again",
            secondAction.toastMessage.contains("Lỗi kết nối máy chủ")
        )
    }

    @Test
    fun testManualReloadTriggersWifiEvenWhenGuardFlagIsTrue() {
        // Simulate previous auto-open occurred
        guard.hasAutoOpenedWifiOnFailure = true

        val manualException = IOException("Network stream reset")
        val action = guard.handleFetchError(manualException, isManualReload = true)

        assertTrue("Manual reload by user must trigger Wi-Fi settings even if guard flag was true", action.shouldOpenWifi)
        assertEquals(
            "Toast should prompt user and open Wi-Fi settings",
            "Không thể kết nối đến máy chủ Supabase. Đang mở cài đặt Wi-Fi...",
            action.toastMessage
        )
    }

    @Test
    fun testSuccessfulFetchResetsGuardFlagAndRecordsTimestamp() {
        guard.hasAutoOpenedWifiOnFailure = true
        val timestamp = 987654321L

        guard.onFetchSuccess(timestamp = timestamp)

        assertFalse("Guard flag must be reset to false on successful Supabase fetch", guard.hasAutoOpenedWifiOnFailure)
        assertEquals("Timestamp must be recorded on successful fetch", timestamp, guard.lastSuccessfulFetchTime)

        // Next failure after success should trigger auto-open again
        val subsequentException = UnknownHostException("DNS failure")
        val action = guard.handleFetchError(subsequentException, isManualReload = false)
        assertTrue("After reset, the next network failure should trigger Wi-Fi auto-open", action.shouldOpenWifi)
        assertTrue(guard.hasAutoOpenedWifiOnFailure)
    }

    @Test
    fun testShouldThrottleFetchCalculations() {
        // Initial state with 0L timestamp
        assertFalse("Should not throttle when lastSuccessfulFetchTime is 0", guard.shouldThrottleFetch(currentTime = 1000L))

        // Set fetch timestamp
        guard.onFetchSuccess(timestamp = 10_000L)

        // Within threshold (< 5000ms)
        assertTrue("Should throttle within threshold", guard.shouldThrottleFetch(currentTime = 12_000L, thresholdMs = 5000L))
        assertTrue("Should throttle at 4999ms delta", guard.shouldThrottleFetch(currentTime = 14_999L, thresholdMs = 5000L))

        // Beyond threshold (>= 5000ms)
        assertFalse("Should not throttle at 5000ms delta", guard.shouldThrottleFetch(currentTime = 15_000L, thresholdMs = 5000L))
        assertFalse("Should not throttle at 6000ms delta", guard.shouldThrottleFetch(currentTime = 16_000L, thresholdMs = 5000L))
    }

    @Test
    fun testNonNetworkExceptionDoesNotTriggerWifiOrSetGuardFlag() {
        val nonNetworkException = IllegalStateException("Corrupt response format")
        val action = guard.handleFetchError(nonNetworkException, isManualReload = false)

        assertFalse("Non-network exception should not trigger Wi-Fi settings", action.shouldOpenWifi)
        assertFalse("Guard flag should remain false for non-network errors", guard.hasAutoOpenedWifiOnFailure)
        assertNull("Location message should not be modified for non-network errors", action.locationMessage)
        assertTrue("Toast message should report general error", action.toastMessage.startsWith("Lỗi tải:"))
    }
}
