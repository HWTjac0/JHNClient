package com.example.hackernews_client.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlgoliaItem(
    val id: Int,
    @param:Json(name = "created_at") val createdAt: String,
    val author: String?,
    val title: String?,
    val url: String?,
    val text: String?,
    val points: Int?,
    @param:Json(name = "parent_id") val parentId: Int?,
    val children: List<AlgoliaItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AlgoliaUser(
    val username: String,
    val about: String?,
    val karma: Int
)

@JsonClass(generateAdapter = true)
data class AlgoliaSearchResponse(
    val hits: List<AlgoliaHit>,
    val page: Int,
    val nbHits: Int,
    val nbPages: Int,
    val hitsPerPage: Int,
    val processingTimeMS: Int,
    val query: String,
    val params: String
)

@JsonClass(generateAdapter = true)
data class AlgoliaHit(
    val objectID: String,
    val title: String?,
    val url: String?,
    val author: String?,
    val points: Int?,
    @param:Json(name = "story_text") val storyText: String?,
    @param:Json(name = "comment_text") val commentText: String?,
    @param:Json(name = "num_comments") val numComments: Int?,
    @param:Json(name = "_tags") val tags: List<String> = emptyList(),
)
