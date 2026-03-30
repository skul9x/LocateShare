package com.skul9x.locateshare.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// ============ Data Classes ============

data class CurrentLocation(
    @SerializedName("id") val id: Int = 1,
    @SerializedName("url") val url: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class FavoriteLocation(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("is_starred") val isStarred: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class UpdateCurrentLocation(
    @SerializedName("url") val url: String,
    @SerializedName("name") val name: String
)

data class InsertFavorite(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
    @SerializedName("is_starred") val isStarred: Boolean = false
)

data class UpdateFavorite(
    @SerializedName("name") val name: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("is_starred") val isStarred: Boolean? = null
)

data class UpdateStarred(
    @SerializedName("is_starred") val isStarred: Boolean
)

// ============ API Interface ============

interface ApiService {

    // --- Current Location ---

    @GET("current_location")
    suspend fun getCurrentLocation(
        @Query("select") select: String = "*",
        @Query("id") id: String = "eq.1"
    ): List<CurrentLocation>

    @PATCH("current_location")
    suspend fun updateCurrentLocation(
        @Query("id") id: String = "eq.1",
        @Body body: UpdateCurrentLocation
    ): ResponseBody

    // --- Favorite Locations ---

    @GET("favorite_locations")
    suspend fun getFavorites(
        @Query("select") select: String = "*",
        @Query("order") order: String = "is_starred.desc,created_at.desc"
    ): List<FavoriteLocation>

    @GET("favorite_locations")
    suspend fun getStarredFavorite(
        @Query("select") select: String = "*",
        @Query("is_starred") isStarred: String = "eq.true",
        @Query("limit") limit: Int = 1
    ): List<FavoriteLocation>

    @POST("favorite_locations")
    suspend fun addFavorite(
        @Body body: InsertFavorite
    ): ResponseBody

    @PATCH("favorite_locations")
    suspend fun updateFavorite(
        @Query("id") id: String,
        @Body body: UpdateFavorite
    ): ResponseBody

    @PATCH("favorite_locations")
    suspend fun unstarAll(
        @Query("is_starred") isStarred: String = "eq.true",
        @Body body: UpdateStarred
    ): ResponseBody

    @DELETE("favorite_locations")
    suspend fun deleteFavorite(
        @Query("id") id: String
    ): ResponseBody
}

// ============ Retrofit Client ============

object RetrofitClient {

    private var retrofit: Retrofit? = null

    private val gson = com.google.gson.GsonBuilder()
        .setLenient()
        .create()

    fun getClient(): Retrofit {
        if (retrofit == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .addHeader("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .build()
                    chain.proceed(request)
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    fun getApiService(): ApiService {
        return getClient().create(ApiService::class.java)
    }
}

// ============ Logger ============

object AppLogger {
    private val logs = StringBuilder()

    fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        logs.append("[$timestamp] $message\n\n")
    }

    fun getLogs(): String {
        return logs.toString()
    }

    fun clear() {
        logs.setLength(0)
    }
}
