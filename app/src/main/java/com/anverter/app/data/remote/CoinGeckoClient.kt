package com.anverter.app.data.remote

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Fetches the raw `/exchange_rates` body. One request covers fiat, crypto and commodities,
 * all anchored to BTC. Network is enqueued (non-blocking) and cancelled with the coroutine.
 */
class CoinGeckoClient(
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun fetchRaw(): String = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url(ENDPOINT)
            .header("Accept", "application/json")
            .build()
        val call = client.newCall(request)

        continuation.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException("HTTP ${resp.code}"))
                        }
                        return
                    }
                    val body = resp.body?.string()
                    if (body == null) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(IOException("Empty response body"))
                        }
                    } else if (continuation.isActive) {
                        continuation.resume(body)
                    }
                }
            }
        })
    }

    companion object {
        const val ENDPOINT = "https://api.coingecko.com/api/v3/exchange_rates"
    }
}
