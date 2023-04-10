package com.onlinemusic.wemu.retrofit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.onlinemusic.wemu.internet.CheckConnectivity.Companion.context
import okhttp3.Cache
import okhttp3.Interceptor
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
   // private const val BASE_URL = "https://developer.shyamfuture.in/wemuonline/public/api/"
    private const val BASE_URL = "https://wemu.online/webadmin/public/api/"
    private var retrofit: Retrofit? = null
    private val cacheSize = (5 * 1024 * 1024).toLong()
    fun hasNetwork(context: Context): Boolean? {
        var isConnected: Boolean? = false // Initial Value
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }
    val retrofitInstance: Retrofit?
        get() {
            if (retrofit == null) {

                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                }
                val cacheInterceptor=Interceptor{ chain ->

                    // Get the request from the chain.
                    var request = chain.request()

                    /*
                    *  Leveraging the advantage of using Kotlin,
                    *  we initialize the request and change its header depending on whether
                    *  the device is connected to Internet or not.
                    */
                    request = if (context?.let { hasNetwork(it) }!!)
                    /*
                    *  If there is Internet, get the cache that was stored 5 seconds ago.
                    *  If the cache is older than 5 seconds, then discard it,
                    *  and indicate an error in fetching the response.
                    *  The 'max-age' attribute is responsible for this behavior.
                    */
                        request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build()
                    else
                    /*
                    *  If there is no Internet, get the cache that was stored 7 days ago.
                    *  If the cache is older than 7 days, then discard it,
                    *  and indicate an error in fetching the response.
                    *  The 'max-stale' attribute is responsible for this behavior.
                    *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
                    */
                        request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 28).build()
                    // End of if-else statement

                    // Add the modified request to the chain.
                    chain.proceed(request)
                }
//
                val myCache = context?.cacheDir?.let { Cache(it, cacheSize) }
                val okHttpClient = OkHttpClient.Builder()
                    .cache(myCache)
                    .readTimeout(3000, TimeUnit.MINUTES)
                    .writeTimeout(3000, TimeUnit.MINUTES)
                    .connectTimeout(3000, TimeUnit.MINUTES)
                    .callTimeout(3000, TimeUnit.SECONDS)
                    .addInterceptor(cacheInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build()
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
            }
            return retrofit
        }
}