package com.skul9x.locateshare.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DoubleTapHandlerTest {

    private class FakeScheduler : DoubleTapHandler.Scheduler {
        data class ScheduledTask(val runnable: Runnable, val triggerTime: Long)

        val queue = mutableListOf<ScheduledTask>()
        var currentTime: Long = 0L

        override fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean {
            queue.add(ScheduledTask(runnable, currentTime + delayMillis))
            return true
        }

        override fun removeCallbacks(runnable: Runnable) {
            queue.removeAll { it.runnable == runnable }
        }

        fun advanceTimeBy(delayMillis: Long) {
            currentTime += delayMillis
            val tasksToRun = queue.filter { it.triggerTime <= currentTime }
            queue.removeAll(tasksToRun)
            for (task in tasksToRun) {
                task.runnable.run()
            }
        }
    }

    private lateinit var fakeScheduler: FakeScheduler
    private var singleTapCount = 0
    private var doubleTapCount = 0
    private lateinit var handler: DoubleTapHandler

    @Before
    fun setUp() {
        fakeScheduler = FakeScheduler()
        singleTapCount = 0
        doubleTapCount = 0
        handler = DoubleTapHandler(
            timeoutMs = 300L,
            timeProvider = { fakeScheduler.currentTime },
            scheduler = fakeScheduler,
            onSingleTap = { singleTapCount++ },
            onDoubleTap = { doubleTapCount++ }
        )
    }

    @Test
    fun testSingleTapTriggersSingleTapCallbackAfterTimeout() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        // Before timeout elapsed: no callbacks triggered yet
        assertEquals("Single tap should not execute immediately", 0, singleTapCount)
        assertEquals("Double tap should not execute on single tap", 0, doubleTapCount)
        assertTrue("Pending runnable should be active", handler.hasPendingSingleTap())

        // Advance time partially (299ms)
        fakeScheduler.advanceTimeBy(299L)
        assertEquals("Single tap should not execute before 300ms timeout", 0, singleTapCount)

        // Advance remaining 1ms (reaching 300ms total)
        fakeScheduler.advanceTimeBy(1L)
        assertEquals("Single tap should execute exactly once after timeout", 1, singleTapCount)
        assertEquals("Double tap should not execute", 0, doubleTapCount)
        assertFalse("Pending runnable should be cleared after execution", handler.hasPendingSingleTap())
    }

    @Test
    fun testDoubleTapTriggersDoubleTapCallbackAndCancelsSingleTap() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        assertEquals(0, singleTapCount)
        assertEquals(0, doubleTapCount)
        assertTrue(handler.hasPendingSingleTap())

        // Second click within 150ms (< 300ms timeout)
        fakeScheduler.advanceTimeBy(150L) // currentTime = 1150L
        handler.processClick(1150L)

        // Double tap executes immediately
        assertEquals("Double tap callback must execute immediately", 1, doubleTapCount)
        assertEquals("Single tap callback must not execute", 0, singleTapCount)
        assertFalse("Pending single tap runnable must be cancelled", handler.hasPendingSingleTap())

        // Advance time well past original timeout
        fakeScheduler.advanceTimeBy(500L)
        assertEquals("Single tap callback must remain suppressed", 0, singleTapCount)
        assertEquals("Double tap callback should not re-trigger", 1, doubleTapCount)
    }

    @Test
    fun testTwoSlowTapsTriggerTwoSingleTapCallbacks() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        // First click finishes its timeout cycle
        fakeScheduler.advanceTimeBy(350L) // currentTime = 1350L
        assertEquals("First tap should execute single tap callback", 1, singleTapCount)
        assertEquals(0, doubleTapCount)

        // Second click happens well after timeout
        fakeScheduler.advanceTimeBy(150L) // currentTime = 1500L
        handler.processClick(1500L)
        assertTrue(handler.hasPendingSingleTap())

        fakeScheduler.advanceTimeBy(350L) // currentTime = 1850L
        assertEquals("Second slow tap should execute single tap callback", 2, singleTapCount)
        assertEquals("Double tap should not trigger on slow taps", 0, doubleTapCount)
    }

    @Test
    fun testRapidTripleTapTriggersDoubleTapThenPendingSingleTap() {
        fakeScheduler.currentTime = 1000L

        // Tap 1
        handler.processClick(1000L)
        fakeScheduler.advanceTimeBy(100L) // currentTime = 1100L

        // Tap 2 (triggers double tap)
        handler.processClick(1100L)
        assertEquals("Double tap should trigger after 2nd rapid tap", 1, doubleTapCount)
        assertEquals(0, singleTapCount)
        assertFalse(handler.hasPendingSingleTap())

        fakeScheduler.advanceTimeBy(100L) // currentTime = 1200L

        // Tap 3 (should start a new single tap cycle)
        handler.processClick(1200L)
        assertTrue("3rd tap should initiate a new pending single tap", handler.hasPendingSingleTap())
        assertEquals(1, doubleTapCount)
        assertEquals(0, singleTapCount)

        // Advance past timeout of 3rd tap
        fakeScheduler.advanceTimeBy(300L) // currentTime = 1500L
        assertEquals("3rd tap should finish as a single tap callback", 1, singleTapCount)
        assertEquals("Double tap count should remain 1", 1, doubleTapCount)
    }

    @Test
    fun testCancelPendingTapsPreventsCallbackExecution() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        assertTrue(handler.hasPendingSingleTap())
        handler.cancelPending()

        assertFalse("Pending state should be cleared", handler.hasPendingSingleTap())
        assertEquals("Last click time should be reset", 0L, handler.getLastClickTime())

        fakeScheduler.advanceTimeBy(500L)
        assertEquals("Single tap callback must not execute after cancelPending", 0, singleTapCount)
        assertEquals("Double tap callback must not execute", 0, doubleTapCount)
    }

    @Test
    fun testOnClickTriggersViaViewListener() {
        fakeScheduler.currentTime = 2000L
        handler.onClick(null)

        assertTrue(handler.hasPendingSingleTap())
        fakeScheduler.advanceTimeBy(300L)

        assertEquals(1, singleTapCount)
        assertEquals(0, doubleTapCount)
    }

    @Test
    fun testDefaultSchedulerFallbackWithoutCrashing() {
        val defaultScheduler = DoubleTapHandler.DefaultScheduler { null }
        val testRunnable = Runnable {}
        val posted = defaultScheduler.postDelayed(testRunnable, 100L)
        assertFalse("Posting without handler should return false", posted)
        defaultScheduler.removeCallbacks(testRunnable)
    }

    @Test
    fun testMultipleSequentialDoubleTaps() {
        fakeScheduler.currentTime = 1000L

        // Double Tap 1
        handler.processClick(1000L)
        fakeScheduler.advanceTimeBy(100L)
        handler.processClick(1100L)
        assertEquals(1, doubleTapCount)
        assertEquals(0, singleTapCount)

        fakeScheduler.advanceTimeBy(500L) // currentTime = 1600L

        // Double Tap 2
        handler.processClick(1600L)
        fakeScheduler.advanceTimeBy(100L)
        handler.processClick(1700L)
        assertEquals(2, doubleTapCount)
        assertEquals(0, singleTapCount)

        fakeScheduler.advanceTimeBy(500L)
        assertEquals(2, doubleTapCount)
        assertEquals(0, singleTapCount)
    }
}
