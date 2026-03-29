package com.example.hackernews_client.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HNItem(
    val id: Int,
    val type: String?, // "job", "story", "comment", "poll", or "pollopt"
    val by: String? = null,
    val time: Long? = null,
    val text: String? = null,
    val dead: Boolean = false,
    val deleted: Boolean = false,
    val parent: Int? = null,
    val poll: Int? = null,
    val kids: List<Int>? = null,
    val url: String? = null,
    val score: Int? = null,
    val title: String? = null,
    val parts: List<Int>? = null,
    val descendants: Int? = null
)

@JsonClass(generateAdapter = true)
data class HNUser(
    val id: String,
    val created: Long,
    val karma: Int,
    val about: String? = null,
    val submitted: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class HNUpdates(
    val items: List<Int>,
    val profiles: List<String>
)
