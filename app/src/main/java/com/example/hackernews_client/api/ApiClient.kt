package com.example.hackernews_client.api

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File

// Api for stories
interface HNFirebaseService {
    @GET("item/{id}.json")
    suspend fun getItem(@Path("id") id: Int): HNItem

    @GET("user/{id}.json")
    suspend fun getUser(@Path("id") id: String): HNUser

    @GET("topstories.json")
    suspend fun getTopStories(): List<Int>

    @GET("newstories.json")
    suspend fun getNewStories(): List<Int>

    @GET("jobstories.json")
    suspend fun getJobStories(): List<Int>

    @GET("updates.json")
    suspend fun getUpdates(): HNUpdates

    @GET("maxitem.json")
    suspend fun getMaxItem(): Int

}

// Api for search
interface HNAlgoliaService {
    @GET("items/{id}")
    suspend fun getItemWithComments(@Path("id") id: Int): AlgoliaItem

    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): AlgoliaUser

    @GET("search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int? = 0
    ): AlgoliaSearchResponse

    @GET("search_by_date")
    suspend fun searchByDate(
        @Query("query") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int? = 0
    ): AlgoliaSearchResponse
}

object ApiClient {
    private var okHttpClient: OkHttpClient? = null

    fun init(context: Context) {
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)
        
        okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                val request = chain.request()
                chain.proceed(request)
            }
            .build()
    }

    fun getClient(baseUrl: String): Retrofit {
        val moshi = Moshi.Builder().build()
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
        
        okHttpClient?.let {
            builder.client(it)
        }
        
        return builder.build()
    }

    inline fun <reified T> createService(baseUrl: String): T {
        return getClient(baseUrl).create(T::class.java)
    }
}

object FirebaseHN {
    private const val BASE_URL = "https://hacker-news.firebaseio.com/v0/"
    val service: HNFirebaseService by lazy {
        ApiClient.createService<HNFirebaseService>(BASE_URL)
    }
}
object AlgoliaHN {
    private const val BASE_URL = "https://hn.algolia.com/api/v1/"
    val service: HNAlgoliaService by lazy {
        ApiClient.createService<HNAlgoliaService>(BASE_URL)
    }
}
