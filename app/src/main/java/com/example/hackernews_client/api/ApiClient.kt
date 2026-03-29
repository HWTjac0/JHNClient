package com.example.hackernews_client.api

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Api for stories
interface HNFirebaseService {
    @GET("v0/item/{id}.json")
    suspend fun getItem(@Path("id") id: Int): HNItem

    @GET("v0/user/{id}.json")
    suspend fun getUser(@Path("id") id: String): HNUser

    @GET("v0/topstories.json")
    suspend fun getTopStories(): List<Int>

    @GET("v0/newstories.json")
    suspend fun getNewStories(): List<Int>

    @GET("v0/jobstories.json")
    suspend fun getJobStories(): List<Int>

    @GET("v0/updates.json")
    suspend fun getUpdates(): HNUpdates

    @GET("v0/maxitem.json")
    suspend fun getMaxItem(): Int

}

// Api for search
interface HNAlgoliaService {
    @GET("api/v1/items/{id}")
    suspend fun getItemWithComments(@Path("id") id: Int): AlgoliaItem

    @GET("api/v1/users/{username}")
    suspend fun getUser(@Path("username") username: String): AlgoliaUser

    @GET("api/v1/search")
    suspend fun search(
        @Query("query") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int? = 0
    ): AlgoliaSearchResponse

    @GET("api/v1/search_by_date")
    suspend fun searchByDate(
        @Query("query") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("page") page: Int? = 0
    ): AlgoliaSearchResponse
}

object ApiClient {
    fun getClient(baseUrl: String): Retrofit {
        val moshi = Moshi.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit
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
    private const val BASE_URL = "http://hn.algolia.com/api/v1/"
    val service: HNAlgoliaService by lazy {
        ApiClient.createService<HNAlgoliaService>(BASE_URL)
    }
}
