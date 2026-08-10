package com.skul9x.locateshare.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper

/**
 * Concrete implementation of [INetworkConnectivityObserver] using [ConnectivityManager.NetworkCallback].
 * Incorporates a rising-edge trigger to fire notifications strictly on disconnected -> connected transitions.
 */
class NetworkConnectivityObserver(
    context: Context,
    private val registrar: CallbackRegistrar = ConnectivityManagerRegistrar(context),
    private val mainExecutor: ((Runnable) -> Unit)? = null
) : INetworkConnectivityObserver {

    interface CallbackRegistrar {
        fun register(callback: ConnectivityManager.NetworkCallback): Boolean
        fun unregister(callback: ConnectivityManager.NetworkCallback)
    }

    class ConnectivityManagerRegistrar(
        private val contextOrCm: Any?
    ) : CallbackRegistrar {
        constructor(cm: ConnectivityManager?) : this(cm as Any?)
        constructor(context: Context) : this(context as Any?)

        private fun getConnectivityManager(): ConnectivityManager? {
            return when (val target = contextOrCm) {
                is ConnectivityManager -> target
                is Context -> {
                    try {
                        target.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                            ?: target.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    } catch (_: Throwable) {
                        null
                    }
                }
                else -> null
            }
        }

        override fun register(callback: ConnectivityManager.NetworkCallback): Boolean {
            val cm = getConnectivityManager() ?: return false
            return try {
                cm.registerDefaultNetworkCallback(callback)
                true
            } catch (e: Exception) {
                false
            }
        }

        override fun unregister(callback: ConnectivityManager.NetworkCallback) {
            val cm = getConnectivityManager() ?: return
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // Safe cleanup if already unregistered or not registered
            } catch (_: Exception) {
                // Prevent any unexpected exceptions during unregistration
            }
        }
    }

    private val lock = Any()

    @Volatile
    private var isListening: Boolean = false
    private var wasConnected: Boolean = false
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var listenerAction: (() -> Unit)? = null

    override fun startListening(onInternetRestored: () -> Unit) {
        synchronized(lock) {
            if (isListening) {
                stopListening()
            }
            listenerAction = onInternetRestored
            wasConnected = false

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    processCapabilitiesChange(hasInternet, isValidated)
                }

                override fun onLost(network: Network) {
                    handleNetworkLost()
                }

                override fun onUnavailable() {
                    handleNetworkLost()
                }
            }

            callback = networkCallback
            val registered = try {
                registrar.register(networkCallback)
            } catch (e: Exception) {
                false
            }

            if (registered) {
                isListening = true
            } else {
                callback = null
                isListening = false
            }
        }
    }

    override fun stopListening() {
        synchronized(lock) {
            val cb = callback
            callback = null
            listenerAction = null
            wasConnected = false
            isListening = false

            if (cb != null) {
                try {
                    registrar.unregister(cb)
                } catch (_: IllegalArgumentException) {
                    // Safe cleanup if already unregistered or not registered
                } catch (_: Exception) {
                    // Prevent any unexpected exceptions during unregistration
                }
            }
        }
    }

    override fun isListening(): Boolean {
        return isListening
    }

    internal fun processCapabilitiesChange(hasInternet: Boolean, isValidated: Boolean) {
        synchronized(lock) {
            val isConnected = hasInternet && isValidated
            if (isConnected) {
                if (!wasConnected) {
                    wasConnected = true
                    val action = listenerAction
                    if (action != null) {
                        dispatchToMain(action)
                    }
                }
            } else {
                wasConnected = false
            }
        }
    }

    internal fun handleNetworkLost() {
        synchronized(lock) {
            wasConnected = false
        }
    }

    internal fun getActiveCallback(): ConnectivityManager.NetworkCallback? {
        synchronized(lock) {
            return callback
        }
    }

    internal fun isWasConnected(): Boolean {
        synchronized(lock) {
            return wasConnected
        }
    }

    private fun dispatchToMain(action: () -> Unit) {
        if (mainExecutor != null) {
            mainExecutor.invoke(Runnable { action() })
        } else {
            try {
                val looper = Looper.getMainLooper()
                if (looper != null) {
                    Handler(looper).post { action() }
                } else {
                    action()
                }
            } catch (_: Throwable) {
                action()
            }
        }
    }
}
