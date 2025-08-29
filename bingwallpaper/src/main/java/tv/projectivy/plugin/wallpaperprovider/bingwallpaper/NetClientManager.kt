package tv.projectivy.plugin.wallpaperprovider.bingwallpaper


import android.content.Context
import android.net.TrafficStats
import android.util.Log
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetClientManager {

    private const val CALL_TIMEOUT_IN_S = 5L
    private const val CACHE_SIZE = 10 * 1024 * 1024L
    private const val MAX_AGE_IN_DAYS = 3

    private val NO_OVERRIDE_CACHE_CONTROL_HEADERS = listOf("no-store", "no-cache", "must-revalidate", "max-age=")

    private val cacheControl: CacheControl
        get() = CacheControl.Builder()
            .maxAge(MAX_AGE_IN_DAYS, TimeUnit.DAYS)
            .build()

    lateinit var httpClient: OkHttpClient

    fun init(context: Context) {
        TrafficStats.setThreadStatsTag(1906)
        httpClient = OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, CACHE_SIZE))
            .callTimeout(CALL_TIMEOUT_IN_S, TimeUnit.SECONDS)
            .addNetworkInterceptor(CacheInterceptor())
            .build()
    }

    fun request(url: String): String? {
        val request: Request = Request.Builder()
            .url(url)
            .build()

        return newCall(request)
    }

    fun newCall(request: Request): String? {
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return response.body?.string()
                }
                Log.d("NetClientManager", "Request unsuccessful. Response code:" + response.code)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    class CacheInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val response: Response = chain.proceed(request)

            // Don't add custom Cache-Control header when not needed
            val existingCacheControl = response.header("Cache-Control")
            existingCacheControl
                ?.takeIf { header ->
                    NO_OVERRIDE_CACHE_CONTROL_HEADERS
                        .any { header.contains(it) }
                }
                ?.let { return response }

            return response.newBuilder()
                .removeHeader("Cache-Control")
                .addHeader("Cache-Control", cacheControl.toString())
                .build()
        }
    }

}
