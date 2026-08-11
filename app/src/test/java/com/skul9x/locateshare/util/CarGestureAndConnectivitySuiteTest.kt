package com.skul9x.locateshare.util

import android.content.Intent
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.skul9x.locateshare.CarActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarGestureAndConnectivitySuiteTest {

    private class TestScheduler : DoubleTapHandler.Scheduler {
        data class Task(val runnable: Runnable, val triggerTime: Long)

        val queue = mutableListOf<Task>()
        var currentTime: Long = 0L

        override fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean {
            queue.add(Task(runnable, currentTime + delayMillis))
            return true
        }

        override fun removeCallbacks(runnable: Runnable) {
            queue.removeAll { it.runnable == runnable }
        }

        fun advanceTimeBy(millis: Long) {
            currentTime += millis
            val tasksToRun = queue.filter { it.triggerTime <= currentTime }
            queue.removeAll(tasksToRun)
            for (task in tasksToRun) {
                task.runnable.run()
            }
        }
    }

    private class TestableCarActivity : CarActivity() {
        var fetchCallCount = 0
        var openMapUrl: String? = null

        override fun fetchCurrentLocation(isManualReload: Boolean) {
            fetchCallCount++
        }

        override fun openMap(url: String) {
            openMapUrl = url
        }
    }

    private lateinit var scheduler: TestScheduler

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
        scheduler = TestScheduler()
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun testDoubleTapConstantsCalibratedForAutomotive() {
        assertEquals(
            "DEFAULT_TIMEOUT_MS must be 320ms for car vibration tolerance",
            320L,
            DoubleTapHandler.DEFAULT_TIMEOUT_MS
        )
        assertEquals(
            "DEFAULT_DOUBLE_TAP_TIMEOUT_MS must be 320ms for car vibration tolerance",
            320L,
            DoubleTapHandler.DEFAULT_DOUBLE_TAP_TIMEOUT_MS
        )
    }

    @Test
    fun testAutomotiveVibrationStaggeredTapsRegisterAsDoubleTap() {
        var singleTapCount = 0
        var doubleTapCount = 0

        val handler = DoubleTapHandler(
            timeoutMs = DoubleTapHandler.DEFAULT_DOUBLE_TAP_TIMEOUT_MS,
            timeProvider = { scheduler.currentTime },
            scheduler = scheduler,
            onSingleTap = { singleTapCount++ },
            onDoubleTap = { doubleTapCount++ }
        )

        // Tap 1 at t = 1000ms
        scheduler.currentTime = 1000L
        handler.processClick(1000L)
        assertTrue(handler.hasPendingSingleTap())
        assertEquals(0, singleTapCount)
        assertEquals(0, doubleTapCount)

        // Road vibration delay: tap 2 at t = 1250ms (250ms delta <= 320ms window)
        scheduler.advanceTimeBy(250L)
        handler.processClick(1250L)

        // Double tap triggers immediately
        assertEquals(1, doubleTapCount)
        assertEquals(0, singleTapCount)
        assertFalse(handler.hasPendingSingleTap())

        // Advancing past window does not fire single tap
        scheduler.advanceTimeBy(500L)
        assertEquals(1, doubleTapCount)
        assertEquals(0, singleTapCount)
    }

    @Test
    fun testSlowTapsBeyondCarTimeoutRegisterAsTwoSingleTaps() {
        var singleTapCount = 0
        var doubleTapCount = 0

        val handler = DoubleTapHandler(
            timeoutMs = DoubleTapHandler.DEFAULT_DOUBLE_TAP_TIMEOUT_MS,
            timeProvider = { scheduler.currentTime },
            scheduler = scheduler,
            onSingleTap = { singleTapCount++ },
            onDoubleTap = { doubleTapCount++ }
        )

        // Tap 1 at t = 1000ms
        scheduler.currentTime = 1000L
        handler.processClick(1000L)

        // Wait past 320ms (e.g. 330ms) -> single tap triggers
        scheduler.advanceTimeBy(330L)
        assertEquals(1, singleTapCount)
        assertEquals(0, doubleTapCount)

        // Tap 2 at t = 1330ms
        handler.processClick(1330L)
        assertTrue(handler.hasPendingSingleTap())

        // Wait past 320ms -> second single tap triggers
        scheduler.advanceTimeBy(330L)
        assertEquals(2, singleTapCount)
        assertEquals(0, doubleTapCount)
    }

    @Test
    fun testCarActivityInternetRestoredTriggersFetchAndBringsToFront() {
        val activity = TestableCarActivity()
        var onInternetRestoredCalled = false

        val testObserver = object : INetworkConnectivityObserver {
            var callback: (() -> Unit)? = null
            var listening = false

            override fun startListening(onInternetRestored: () -> Unit) {
                listening = true
                callback = onInternetRestored
            }

            override fun stopListening() {
                listening = false
                callback = null
            }

            override fun isListening(): Boolean = listening
        }

        activity.networkObserver = testObserver
        testObserver.startListening {
            activity.onInternetRestored()
            onInternetRestoredCalled = true
        }

        // Simulate network restoration event
        testObserver.callback?.invoke()

        assertTrue("Internet restoration callback should have been triggered", onInternetRestoredCalled)
        assertEquals("fetchCurrentLocation should be called on internet restoration", 1, activity.fetchCallCount)
        assertFalse("Network observer should stop listening after restoration", testObserver.isListening())
    }

    @Test
    fun testCarActivityBringToFrontFlags() {
        val expectedFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        assertEquals(
            "Bring to front intent flags constant must contain REORDER_TO_FRONT and SINGLE_TOP flags",
            expectedFlags,
            CarActivity.BRING_TO_FRONT_FLAGS
        )
    }

    @Test
    fun testConnectionGuardThrottleLogic() {
        val guard = SupabaseConnectionGuard()

        val t0 = 10000L
        assertFalse("Initial fetch should not be throttled", guard.shouldThrottleFetch(t0))

        guard.onFetchSuccess(t0)
        assertTrue("Fetch immediately after success (< 5000ms) should be throttled", guard.shouldThrottleFetch(t0 + 1000L))
        assertFalse("Fetch after 5000ms throttle interval should be allowed", guard.shouldThrottleFetch(t0 + 5500L))
    }
}
