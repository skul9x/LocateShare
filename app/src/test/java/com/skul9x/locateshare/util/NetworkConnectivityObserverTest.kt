package com.skul9x.locateshare.util

import android.content.Context
import android.content.ContextWrapper
import android.net.ConnectivityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NetworkConnectivityObserverTest {

    private lateinit var mockContext: Context
    private lateinit var fakeRegistrar: FakeCallbackRegistrar

    class FakeCallbackRegistrar : NetworkConnectivityObserver.CallbackRegistrar {
        var registeredCallback: ConnectivityManager.NetworkCallback? = null
        var unregisterCallCount: Int = 0
        var shouldFailOnRegister: Boolean = false
        var shouldThrowOnUnregister: Boolean = false

        override fun register(callback: ConnectivityManager.NetworkCallback): Boolean {
            if (shouldFailOnRegister) {
                return false
            }
            registeredCallback = callback
            return true
        }

        override fun unregister(callback: ConnectivityManager.NetworkCallback) {
            unregisterCallCount++
            if (shouldThrowOnUnregister) {
                throw IllegalArgumentException("Callback was not registered")
            }
            if (registeredCallback == callback) {
                registeredCallback = null
            }
        }
    }

    @Before
    fun setUp() {
        mockContext = ContextWrapper(null)
        fakeRegistrar = FakeCallbackRegistrar()
    }

    @Test
    fun testObserverInitialStateNotListening() {
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        assertFalse("Observer must start with isListening == false", observer.isListening())
        assertNull("Active callback must be null before startListening()", observer.getActiveCallback())
        assertFalse("wasConnected flag should initially be false", observer.isWasConnected())
    }

    @Test
    fun testStartListeningSetsActiveState() {
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        var callbackFired = false
        observer.startListening {
            callbackFired = true
        }

        assertTrue("Observer must report isListening == true after startListening()", observer.isListening())
        assertNotNull("Registrar should have registered callback", fakeRegistrar.registeredCallback)
        assertFalse("Callback should not fire immediately before network capability event", callbackFired)
    }

    @Test
    fun testStartListeningHandlesRegistrationFailureGracefully() {
        fakeRegistrar.shouldFailOnRegister = true
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        observer.startListening {}

        assertFalse("isListening must be false if registrar fails", observer.isListening())
        assertNull("Active callback must be null when registration fails", observer.getActiveCallback())
    }

    @Test
    fun testStopListeningCleansUpState() {
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        observer.startListening {}
        assertTrue(observer.isListening())

        observer.stopListening()

        assertFalse("Observer must report isListening == false after stopListening()", observer.isListening())
        assertNull("Active callback must be cleared upon stopListening()", observer.getActiveCallback())
        assertNull("FakeRegistrar should have unregistered callback", fakeRegistrar.registeredCallback)
    }

    @Test
    fun testMultipleStopCallsDoNotThrowException() {
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        fakeRegistrar.shouldThrowOnUnregister = true

        observer.startListening {}
        assertTrue(observer.isListening())

        // First stop with exception-throwing unregister
        observer.stopListening()
        assertFalse(observer.isListening())

        // Multiple subsequent stop calls should execute cleanly and idempotently
        observer.stopListening()
        observer.stopListening()
        assertFalse(observer.isListening())
        assertEquals("Unregister should only be invoked once when callback was non-null", 1, fakeRegistrar.unregisterCallCount)
    }

    @Test
    fun testStartListeningWhenAlreadyListeningCleansUpPreviousCallback() {
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        observer.startListening {}
        val firstCallback = observer.getActiveCallback()
        assertNotNull(firstCallback)

        // Calling startListening again should stop previous and register new
        observer.startListening {}
        val secondCallback = observer.getActiveCallback()
        assertNotNull(secondCallback)
        assertEquals("Previous callback should have been unregistered", 1, fakeRegistrar.unregisterCallCount)
        assertTrue(observer.isListening())
    }

    @Test
    fun testRisingEdgeTriggerFiresCallbackOnceWhenValidated() {
        var restoredCount = 0
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        observer.startListening {
            restoredCount++
        }

        // First capability change: hasInternet=true, isValidated=true (rising edge 0 -> 1)
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        assertEquals("Callback should fire once on first validated connection event", 1, restoredCount)
        assertTrue("wasConnected state should be updated to true", observer.isWasConnected())

        // Subsequent capability updates while still connected (e.g. signal strength changed)
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        assertEquals("Subsequent capability updates while connected must NOT re-fire callback", 1, restoredCount)
    }

    @Test
    fun testDisconnectionResetsStateAndAllowsNextRisingEdge() {
        var restoredCount = 0
        val observer = NetworkConnectivityObserver(mockContext, fakeRegistrar) { runnable -> runnable.run() }
        observer.startListening {
            restoredCount++
        }

        // 1. Initial connection
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        assertEquals(1, restoredCount)

        // 2. Disconnection / lost validation
        observer.processCapabilitiesChange(hasInternet = true, isValidated = false)
        assertFalse("wasConnected should become false when validation is lost", observer.isWasConnected())
        assertEquals("Disconnection does not trigger restoration callback", 1, restoredCount)

        // 3. Re-connection (second rising edge 0 -> 1)
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        assertEquals("Re-connection after loss must fire restored callback again", 2, restoredCount)
        assertTrue(observer.isWasConnected())

        // 4. OnLost resets state
        observer.handleNetworkLost()
        assertFalse("handleNetworkLost must reset wasConnected to false", observer.isWasConnected())

        // 5. Re-connection after onLost
        observer.processCapabilitiesChange(hasInternet = true, isValidated = true)
        assertEquals("Re-connection after handleNetworkLost must fire restored callback", 3, restoredCount)
    }

    @Test
    fun testInterfaceContractMockability() {
        var startCalled = false
        var stopCalled = false
        val mockObserver: INetworkConnectivityObserver = object : INetworkConnectivityObserver {
            private var listening = false
            override fun startListening(onInternetRestored: () -> Unit) {
                startCalled = true
                listening = true
            }
            override fun stopListening() {
                stopCalled = true
                listening = false
            }
            override fun isListening(): Boolean = listening
        }

        assertFalse(mockObserver.isListening())
        mockObserver.startListening {}
        assertTrue(startCalled)
        assertTrue(mockObserver.isListening())
        mockObserver.stopListening()
        assertTrue(stopCalled)
        assertFalse(mockObserver.isListening())
    }
}
