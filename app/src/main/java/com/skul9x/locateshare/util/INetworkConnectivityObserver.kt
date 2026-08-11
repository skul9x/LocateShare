package com.skul9x.locateshare.util

/**
 * Interface defining lifecycle-safe network connectivity monitoring contract.
 */
interface INetworkConnectivityObserver {

    /**
     * Starts listening for network connectivity changes.
     * Invokes [onInternetRestored] on the main thread when validated internet becomes available (rising edge).
     */
    fun startListening(onInternetRestored: () -> Unit)

    /**
     * Stops listening and cleans up callbacks safely without throwing exceptions.
     */
    fun stopListening()

    /**
     * Checks if the observer is actively listening for connectivity changes.
     */
    fun isListening(): Boolean
}
