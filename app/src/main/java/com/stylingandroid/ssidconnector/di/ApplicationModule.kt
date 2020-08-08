package com.stylingandroid.ssidconnector.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.stylingandroid.ssidconnector.WifiConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideWifiManager(@ApplicationContext context: Context): WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Provides
    fun provideWifiConnector(
        wifiManager: WifiManager,
        connectivityManager: ConnectivityManager
    ): WifiConnector =
        WifiConnector.create(wifiManager, connectivityManager)
}
