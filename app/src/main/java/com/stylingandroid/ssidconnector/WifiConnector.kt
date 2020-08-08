@file:Suppress("DEPRECATION")

package com.stylingandroid.ssidconnector

import android.annotation.TargetApi
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkSpecifier
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class WifiConnector constructor(
    protected val wifiManager: WifiManager,
    protected val connectivityManager: ConnectivityManager
) {

    private inner class Callback(private val sendChannel: SendChannel<String>) :
        ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            sendChannel.offer(currentSsid)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            sendChannel.offer(currentSsid)
        }
    }

    @ExperimentalCoroutinesApi
    fun connectivityFlow(): Flow<String> = callbackFlow {
        val callback = Callback(this)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    val currentSsid: String
        get() = wifiManager.connectionInfo
            ?.takeIf { it.supplicantState == SupplicantState.COMPLETED }
            ?.ssid
            ?.replace("\"", "")
            ?: "<None>"

    abstract suspend fun connect(ssid: String, passphrase: String)
    abstract suspend fun disconnect()

    companion object {
        fun create(
            wifiManager: WifiManager,
            connectivityManager: ConnectivityManager
        ): WifiConnector {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                QWifiConnector(wifiManager, connectivityManager)
            } else {
                LegacyWifiConnector(wifiManager, connectivityManager)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private class QWifiConnector(
        wifiManager: WifiManager,
        connectivityManager: ConnectivityManager
    ) : WifiConnector(wifiManager, connectivityManager) {
        private val callback = object : ConnectivityManager.NetworkCallback() {}

        override suspend fun connect(ssid: String, passphrase: String) {
            val networkSpecifier: NetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(passphrase)
                .build()
            val networkRequest: NetworkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(networkSpecifier)
                .build()
            connectivityManager.requestNetwork(networkRequest, callback)
        }

        override suspend fun disconnect() {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    @Suppress("Deprecation")
    private class LegacyWifiConnector(
        wifiManager: WifiManager,
        connectivityManager: ConnectivityManager
    ) : WifiConnector(wifiManager, connectivityManager) {

        private var netId: Int = -1

        override suspend fun connect(ssid: String, passphrase: String) {
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$passphrase\""
            }
            netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.disconnect()
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
        }

        override suspend fun disconnect() {
            if (netId != -1) {
                wifiManager.disconnect()
                wifiManager.removeNetwork(netId)
                netId = -1
                wifiManager.reconnect()
            }
        }
    }
}
