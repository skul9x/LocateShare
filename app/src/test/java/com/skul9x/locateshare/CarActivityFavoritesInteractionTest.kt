package com.skul9x.locateshare

import android.view.View
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import com.skul9x.locateshare.util.DoubleTapHandler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarActivityFavoritesInteractionTest {

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

    class TestableCarActivity : CarActivity() {
        var openStarredFavoriteCallCount = 0
        var showFavoritesPopupCallCount = 0

        override fun openStarredFavorite() {
            openStarredFavoriteCallCount++
        }

        override fun showFavoritesPopup() {
            showFavoritesPopupCallCount++
        }
    }

    private lateinit var testActivity: TestableCarActivity
    private lateinit var fakeScheduler: FakeScheduler
    private lateinit var handler: DoubleTapHandler

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
        fakeScheduler = FakeScheduler()
        handler = testActivity.initFavoritesDoubleTapHandler(
            scheduler = fakeScheduler,
            timeProvider = { fakeScheduler.currentTime }
        )
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    @Test
    fun testSingleTapTriggersOpenStarredFavorite() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        // Before timeout: no method call triggered
        assertEquals("openStarredFavorite should not execute before timeout", 0, testActivity.openStarredFavoriteCallCount)
        assertEquals("showFavoritesPopup should not execute on single tap", 0, testActivity.showFavoritesPopupCallCount)
        assertTrue("Handler should have pending single tap callback", handler.hasPendingSingleTap())

        // Advance time to satisfy timeout
        fakeScheduler.advanceTimeBy(DoubleTapHandler.DEFAULT_TIMEOUT_MS)

        assertEquals("Single tap should trigger openStarredFavorite after timeout", 1, testActivity.openStarredFavoriteCallCount)
        assertEquals("Single tap must not trigger showFavoritesPopup", 0, testActivity.showFavoritesPopupCallCount)
        assertFalse("Pending single tap runnable should be cleared", handler.hasPendingSingleTap())
    }

    @Test
    fun testDoubleTapTriggersFavoritesPopup() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        assertEquals(0, testActivity.openStarredFavoriteCallCount)
        assertEquals(0, testActivity.showFavoritesPopupCallCount)

        // Second click within timeout window (e.g. 150ms)
        fakeScheduler.advanceTimeBy(150L) // currentTime = 1150L
        handler.processClick(1150L)

        assertEquals("Double tap must immediately trigger showFavoritesPopup", 1, testActivity.showFavoritesPopupCallCount)
        assertEquals("openStarredFavorite must not be triggered on double tap", 0, testActivity.openStarredFavoriteCallCount)
        assertFalse("Pending single tap runnable must be cancelled on double tap", handler.hasPendingSingleTap())

        // Advance past original timeout to verify single tap callback remains cancelled
        fakeScheduler.advanceTimeBy(500L)
        assertEquals("openStarredFavorite must not execute after delay", 0, testActivity.openStarredFavoriteCallCount)
        assertEquals("showFavoritesPopup should only have been called once", 1, testActivity.showFavoritesPopupCallCount)
    }

    @Test
    fun testLongClickDoesNotTriggerPopup() {
        // In the previous implementation, setOnLongClickListener called showFavoritesPopup.
        // With DoubleTapHandler replacing it, long-press gestures are not handled or assigned to open the popup.
        // We verify that long clicks or view long-click events do not trigger showFavoritesPopup.
        assertEquals(0, testActivity.showFavoritesPopupCallCount)
        assertEquals(0, testActivity.openStarredFavoriteCallCount)

        // Verify that favoritesDoubleTapHandler is an OnClickListener and not an OnLongClickListener
        assertTrue("favoritesDoubleTapHandler implements View.OnClickListener", View.OnClickListener::class.java.isAssignableFrom(handler.javaClass))
        assertFalse(
            "favoritesDoubleTapHandler should not implement View.OnLongClickListener",
            View.OnLongClickListener::class.java.isAssignableFrom(handler.javaClass)
        )

        // Neither callback should have been triggered
        assertEquals("showFavoritesPopup must not be triggered by long click", 0, testActivity.showFavoritesPopupCallCount)
    }

    @Test
    fun testEmptyStarredFavoriteToastContainsDoubleTapInstruction() {
        val toastMessage = CarActivity.EMPTY_STARRED_TOAST

        assertTrue(
            "Toast message must guide the user with 'Chạm đúp'",
            toastMessage.contains("Chạm đúp")
        )
        assertFalse(
            "Toast message must no longer contain 'Ấn giữ'",
            toastMessage.contains("Ấn giữ")
        )
        assertEquals(
            "Toast message must match exact Vietnamese instruction format",
            "Chưa có địa điểm mặc định ⭐\nChạm đúp để chọn từ danh sách",
            toastMessage
        )
    }

    @Test
    fun testOnDestroyCancelsPendingTapCallbacks() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)
        assertTrue("Handler should have pending tap callback before destroy", handler.hasPendingSingleTap())

        testActivity.handleDestroy()

        assertFalse("handleDestroy must cancel pending tap callbacks to avoid memory leaks", handler.hasPendingSingleTap())

        // Advancing time should not fire any callback after destroy
        fakeScheduler.advanceTimeBy(DoubleTapHandler.DEFAULT_TIMEOUT_MS + 100L)
        assertEquals("openStarredFavorite must not execute after activity destruction", 0, testActivity.openStarredFavoriteCallCount)
    }

    @Test
    fun testSlowTapsTreatedAsTwoSeparateSingleTaps() {
        fakeScheduler.currentTime = 1000L
        handler.processClick(1000L)

        // Advance time past timeout window (e.g. 350ms)
        fakeScheduler.advanceTimeBy(350L) // currentTime = 1350L
        assertEquals("First tap should execute openStarredFavorite after timeout", 1, testActivity.openStarredFavoriteCallCount)

        // Second click after timeout window
        handler.processClick(1350L)
        fakeScheduler.advanceTimeBy(300L) // currentTime = 1650L
        assertEquals("Second tap after timeout should execute openStarredFavorite as second single tap", 2, testActivity.openStarredFavoriteCallCount)
        assertEquals("Double tap popup must not be triggered for slow consecutive taps", 0, testActivity.showFavoritesPopupCallCount)
    }
}
