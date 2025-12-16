package com.skul9x.locateshare.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LocationData(
    @SerializedName("url") val url: String
)

interface ApiService {
    @retrofit2.http.FormUrlEncoded
    @POST("index.php")
    suspend fun sendLocation(
        @retrofit2.http.Field("url") url: String,
        @retrofit2.http.Field("name") name: String
    ): okhttp3.ResponseBody

    @GET("index.php")
    suspend fun getLocation(): okhttp3.ResponseBody
}

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private val gson = com.google.gson.GsonBuilder()
        .setLenient()
        .create()
    
    var cookie: String = ""

    fun getClient(baseUrl: String): Retrofit {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                
                // Identify as App
                requestBuilder.addHeader("X-Requested-With", "com.skul9x.locateshare")
                
                if (cookie.isNotEmpty()) {
                    requestBuilder.addHeader("Cookie", cookie)
                    requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
}

object AppLogger {
    private val logs = StringBuilder()

    fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        logs.append("[$timestamp] $message\n\n")
    }

    fun getLogs(): String {
        return logs.toString()
    }
    
    fun clear() {
        logs.setLength(0)
    }
}
