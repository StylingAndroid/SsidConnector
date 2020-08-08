package com.stylingandroid.ssidconnector.ui.networks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.stylingandroid.ssidconnector.BuildConfig
import com.stylingandroid.ssidconnector.WifiConnector
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class NetworksViewModel @Inject constructor(
    private val wifiConnector: WifiConnector
) : ViewModel() {

    @ExperimentalCoroutinesApi
    val currentWifiSsid: LiveData<String> = wifiConnector.connectivityFlow()
        .onStart { emit(wifiConnector.currentSsid) }
        .asLiveData(viewModelScope.coroutineContext)

    @ExperimentalCoroutinesApi
    val isSpecifiedWifiConnected: LiveData<Boolean> = currentWifiSsid.map { it == SSID }

    @ExperimentalCoroutinesApi
    fun toggleConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSpecifiedWifiConnected.value == true) {
                wifiConnector.disconnect()
            } else {
                wifiConnector.connect(SSID, PASSPHRASE)
            }
        }
    }

    companion object {
        private const val SSID = BuildConfig.SSID
        private const val PASSPHRASE = BuildConfig.PRE_SHARED_KEY
    }
}
