package com.anverter.app.di

import android.content.Context
import com.anverter.app.core.ConnectivityObserver
import com.anverter.app.data.RatesRepository
import com.anverter.app.data.local.RatesCache
import com.anverter.app.data.local.SettingsStore
import com.anverter.app.data.remote.CoinGeckoClient
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/** Manual dependency container - no DI framework keeps the build lean and fast. */
class AppContainer(context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val coinGeckoClient = CoinGeckoClient(httpClient)
    private val ratesCache = RatesCache(context)

    val ratesRepository = RatesRepository(coinGeckoClient, ratesCache)
    val connectivityObserver = ConnectivityObserver(context)
    val settingsStore = SettingsStore(context)
}
