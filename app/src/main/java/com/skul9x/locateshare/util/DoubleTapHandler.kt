package com.skul9x.locateshare.util

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Double-tap gesture handler that differentiates between single-tap and double-tap gestures
 * on Android UI elements without gesture conflicts or timing anomalies.
 *
 * Implements [View.OnClickListener] for direct attachment to Views.
 */
open class DoubleTapHandler(
    private val timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val scheduler: Scheduler = DefaultScheduler(),
    private val onSingleTap: () -> Unit = {},
    private val onDoubleTap: () -> Unit = {}
) : View.OnClickListener {

    /**
     * Abstraction interface for scheduling and cancelling delayed runnables.
     */
    interface Scheduler {
        fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean
        fun removeCallbacks(runnable: Runnable)
    }

    /**
     * Default Android Main Looper scheduler with defensive fallback for unit testing environments.
     */
    class DefaultScheduler(
        private val handlerProvider: () -> Handler? = {
            try {
                val looper = Looper.getMainLooper()
                if (looper != null) Handler(looper) else null
            } catch (_: Throwable) {
                null
            }
        }
    ) : Scheduler {
        private val handler: Handler? by lazy { handlerProvider() }

        override fun postDelayed(runnable: Runnable, delayMillis: Long): Boolean {
            val h = handler
            return if (h != null) {
                try {
                    h.postDelayed(runnable, delayMillis)
                } catch (_: Throwable) {
                    false
                }
            } else {
                false
            }
        }

        override fun removeCallbacks(runnable: Runnable) {
            try {
                handler?.removeCallbacks(runnable)
            } catch (_: Throwable) {
                // Safe ignore
            }
        }
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 300L
    }

    private var lastClickTime: Long = 0L
    private var pendingRunnable: Runnable? = null

    constructor(
        timeoutMs: Long,
        onSingleTap: () -> Unit,
        onDoubleTap: () -> Unit
    ) : this(
        timeoutMs = timeoutMs,
        timeProvider = { System.currentTimeMillis() },
        scheduler = DefaultScheduler(),
        onSingleTap = onSingleTap,
        onDoubleTap = onDoubleTap
    )

    constructor(
        onSingleTap: () -> Unit,
        onDoubleTap: () -> Unit
    ) : this(
        timeoutMs = DEFAULT_TIMEOUT_MS,
        timeProvider = { System.currentTimeMillis() },
        scheduler = DefaultScheduler(),
        onSingleTap = onSingleTap,
        onDoubleTap = onDoubleTap
    )

    /**
     * Primary entry point when attached as an OnClickListener to a View.
     */
    override fun onClick(v: View?) {
        processClick(timeProvider())
    }

    /**
     * Processes a click event at the specified timestamp [currentTimeMs].
     * Can be called directly for deterministic testing or custom event loops.
     */
    @Synchronized
    fun processClick(currentTimeMs: Long = timeProvider()) {
        val pending = pendingRunnable
        if (pending != null && (currentTimeMs - lastClickTime) <= timeoutMs) {
            // Second click arrived within timeout window -> Double Tap!
            cancelPendingInternal()
            onDoubleTap()
        } else {
            // First click or subsequent click after timeout
            cancelPendingInternal()
            lastClickTime = currentTimeMs
            val singleTapRunnable = object : Runnable {
                override fun run() {
                    synchronized(this@DoubleTapHandler) {
                        if (pendingRunnable == this) {
                            pendingRunnable = null
                            lastClickTime = 0L
                            onSingleTap()
                        }
                    }
                }
            }
            pendingRunnable = singleTapRunnable
            scheduler.postDelayed(singleTapRunnable, timeoutMs)
        }
    }

    /**
     * Cancels any pending single-tap callbacks and resets the tap state.
     * Useful during View detachment or Activity destruction to prevent memory leaks and dangling callbacks.
     */
    @Synchronized
    fun cancelPending() {
        cancelPendingInternal()
    }

    @Synchronized
    fun hasPendingSingleTap(): Boolean {
        return pendingRunnable != null
    }

    @Synchronized
    fun getLastClickTime(): Long {
        return lastClickTime
    }

    private fun cancelPendingInternal() {
        pendingRunnable?.let { runnable ->
            scheduler.removeCallbacks(runnable)
        }
        pendingRunnable = null
        lastClickTime = 0L
    }
}
