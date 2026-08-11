package com.skul9x.locateshare

import android.content.Intent
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.skul9x.locateshare.util.INetworkConnectivityObserver
import com.skul9x.locateshare.util.SupabaseConnectionGuard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarActivityAutoDismissWifiTest {

    private lateinit var testActivity: TestableCarActivity
    private lateinit var fakeObserver: FakeNetworkObserver

    class FakeNetworkObserver : INetworkConnectivityObserver {
        var isListeningState: Boolean = false
        var registeredCallback: (() -> Unit)? = null
        var startListeningCallCount: Int = 0
        var stopListeningCallCount: Int = 0

        override fun startListening(onInternetRestored: () -> Unit) {
            isListeningState = true
            startListeningCallCount++
            registeredCallback = onInternetRestored
        }

        override fun stopListening() {
            isListeningState = false
            stopListeningCallCount++
            registeredCallback = null
        }

        override fun isListening(): Boolean = isListeningState

        fun triggerInternetRestored() {
            registeredCallback?.invoke()
        }
    }

    class TestableCarActivity : CarActivity() {
        val startedIntents = mutableListOf<Intent>()
        var fetchLocationCallCount = 0
        var lastManualReloadParam: Boolean? = null

        override fun startActivity(intent: Intent?) {
            if (intent != null) {
                startedIntents.add(intent)
            }
        }

        override fun fetchCurrentLocation(isManualReload: Boolean) {
            fetchLocationCallCount++
            lastManualReloadParam = isManualReload
        }
    }

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean = true
        })

        testActivity = TestableCarActivity()
        fakeObserver = FakeNetworkObserver()
        testActivity.networkObserver = fakeObserver
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun testWifiLaunchStartsReconnectionObserver() {
        assertFalse("Observer should not be listening initially", fakeObserver.isListening())
        assertEquals(0, fakeObserver.startListeningCallCount)

        val opened = testActivity.openWifiSettings()

        assertTrue("openWifiSettings should return true when activity launch succeeds", opened)
        assertTrue("Opening Wi-Fi settings must initiate network observation", fakeObserver.isListening())
        assertEquals("startListening should have been called once", 1, fakeObserver.startListeningCallCount)
        assertNotNull("Reconnection callback action must be registered", fakeObserver.registeredCallback)
    }

    @Test
    fun testReconnectionTriggersBringToFrontIntent() {
        val expectedFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP

        assertEquals(
            "Bring-to-front intent flags constant must include FLAG_ACTIVITY_REORDER_TO_FRONT and FLAG_ACTIVITY_SINGLE_TOP",
            expectedFlags,
            CarActivity.BRING_TO_FRONT_FLAGS
        )
        assertTrue(
            "FLAG_ACTIVITY_REORDER_TO_FRONT must be set in BRING_TO_FRONT_FLAGS",
            (CarActivity.BRING_TO_FRONT_FLAGS and Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) != 0
        )
        assertTrue(
            "FLAG_ACTIVITY_SINGLE_TOP must be set in BRING_TO_FRONT_FLAGS",
            (CarActivity.BRING_TO_FRONT_FLAGS and Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0
        )

        val intent = testActivity.createBringToFrontIntent()
        assertNotNull("createBringToFrontIntent should create a valid Intent", intent)

        // Execute onInternetRestored flow
        testActivity.onInternetRestored()

        assertTrue("An intent should have been dispatched via startActivity", testActivity.startedIntents.isNotEmpty())
        assertEquals("fetchCurrentLocation should have been triggered with isManualReload = false", 1, testActivity.fetchLocationCallCount)
        assertEquals(false, testActivity.lastManualReloadParam)
    }

    @Test
    fun testReconnectionStopsObserverToPreventDuplicateSync() {
        fakeObserver.startListening { /* noop */ }
        assertTrue(fakeObserver.isListening())

        testActivity.onInternetRestored()

        assertFalse("Observer must be stopped after internet restoration to avoid duplicate cycles", fakeObserver.isListening())
        assertTrue("stopListening should have been invoked at least once", fakeObserver.stopListeningCallCount >= 1)
    }

    @Test
    fun testOnResumeThrottlesDuplicateFetchWhenRecentlyFetched() {
        val guard = testActivity.connectionGuard
        val baseTime = 100_000L

        // Initial state: lastSuccessfulFetchTime = 0L -> should NOT throttle
        assertFalse(
            "Initial fetch should never be throttled",
            guard.shouldThrottleFetch(currentTime = baseTime, thresholdMs = 5000L)
        )

        // Simulate successful fetch at baseTime
        guard.onFetchSuccess(timestamp = baseTime)
        assertEquals("lastSuccessfulFetchTime must be updated to baseTime", baseTime, guard.lastSuccessfulFetchTime)

        // Scenario 1: onResume runs 1.5 seconds later (within 5000ms threshold) -> MUST throttle
        val resumeTime1 = baseTime + 1500L
        assertTrue(
            "onResume within 5s after auto-dismiss fetch must be throttled to prevent duplicate requests",
            guard.shouldThrottleFetch(currentTime = resumeTime1, thresholdMs = 5000L)
        )

        // Scenario 2: onResume runs 4.999 seconds later -> MUST throttle
        val resumeTime2 = baseTime + 4999L
        assertTrue(
            "onResume just before 5s threshold must still be throttled",
            guard.shouldThrottleFetch(currentTime = resumeTime2, thresholdMs = 5000L)
        )

        // Scenario 3: onResume runs 5.001 seconds later -> MUST NOT throttle
        val resumeTime3 = baseTime + 5001L
        assertFalse(
            "onResume after 5s threshold should allow new fetch",
            guard.shouldThrottleFetch(currentTime = resumeTime3, thresholdMs = 5000L)
        )

        // Scenario 4: onResume runs 10 seconds later -> MUST NOT throttle
        val resumeTime4 = baseTime + 10000L
        assertFalse(
            "onResume well past threshold should allow new fetch",
            guard.shouldThrottleFetch(currentTime = resumeTime4, thresholdMs = 5000L)
        )
    }

    @Test
    fun testOnResumeExecutesFetchWhenNotThrottled() {
        testActivity.fetchLocationCallCount = 0
        testActivity.connectionGuard.lastSuccessfulFetchTime = 0L

        testActivity.handleResume()

        assertEquals("handleResume should trigger fetchCurrentLocation when not throttled", 1, testActivity.fetchLocationCallCount)
        assertEquals(false, testActivity.lastManualReloadParam)
    }

    @Test
    fun testOnResumeSkipsFetchWhenThrottled() {
        testActivity.fetchLocationCallCount = 0
        // Set fetch time to current time
        testActivity.connectionGuard.lastSuccessfulFetchTime = System.currentTimeMillis()

        testActivity.handleResume()

        assertEquals("handleResume should skip fetchCurrentLocation when recently fetched", 0, testActivity.fetchLocationCallCount)
    }

    @Test
    fun testOnDestroyCleansUpObserver() {
        fakeObserver.startListening { /* noop */ }
        assertTrue(fakeObserver.isListening())

        testActivity.handleDestroy()

        assertFalse("Activity handleDestroy must stop network observer to prevent leaks", fakeObserver.isListening())
        assertTrue("stopListening must have been called during handleDestroy", fakeObserver.stopListeningCallCount >= 1)
    }

    @Test
    fun testSupabaseConnectionGuardThrottlingEdgeCases() {
        val guard = SupabaseConnectionGuard()

        // With default 0L timestamp
        assertFalse(guard.shouldThrottleFetch(System.currentTimeMillis()))
        assertFalse(guard.shouldThrottleFetch(0L))

        // Custom threshold
        guard.onFetchSuccess(1000L)
        assertTrue(guard.shouldThrottleFetch(1500L, thresholdMs = 1000L))
        assertFalse(guard.shouldThrottleFetch(2000L, thresholdMs = 1000L))
        assertFalse(guard.shouldThrottleFetch(2500L, thresholdMs = 1000L))
    }

    @Test
    fun testSupabaseConnectionGuardOnFetchSuccessResetsBothGuardAndRecordsTimestamp() {
        val guard = SupabaseConnectionGuard(hasAutoOpenedWifiOnFailure = true, lastSuccessfulFetchTime = 0L)
        val timestamp = 123456789L

        guard.onFetchSuccess(timestamp)

        assertFalse("hasAutoOpenedWifiOnFailure must be reset to false", guard.hasAutoOpenedWifiOnFailure)
        assertEquals("lastSuccessfulFetchTime must be updated", timestamp, guard.lastSuccessfulFetchTime)
    }
}
