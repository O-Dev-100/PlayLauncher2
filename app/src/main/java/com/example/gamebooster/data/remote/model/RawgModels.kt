package com.example.gamebooster.data.remote.model

data class RawgGameDetails(
    val id: Int,
    val name: String,
    val description_raw: String?,
    val background_image: String?,
    val website: String?,
    val rating: Double?,
    val genres: List<Genre>?
)

data class Genre(val name: String)

data class GameSearchResponse(
    val results: List<SearchResult> = emptyList()
)

data class SearchResult(
    val id: Int,
    val name: String,
    val background_image: String?
)


