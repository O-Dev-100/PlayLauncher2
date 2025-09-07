package com.example.gamebooster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "game_details")
data class GameDetails(
    @PrimaryKey
    @SerializedName("id") val packageName: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("descriptionHTML") val descriptionHTML: String?,
    @SerializedName("summary") val summary: String,
    @SerializedName("installs") val installs: String,
    @SerializedName("minInstalls") val minInstalls: Long,
    @SerializedName("score") val score: Double,
    @SerializedName("scoreText") val scoreText: String,
    @SerializedName("ratings") val ratings: Int,
    @SerializedName("reviews") val reviews: Int,
    @SerializedName("histogram") val histogram: Map<Int, Int>?,
    @SerializedName("price") val price: Double,
    @SerializedName("free") val free: Boolean,
    @SerializedName("currency") val currency: String,
    @SerializedName("priceText") val priceText: String,
    @SerializedName("offersIAP") val offersIAP: Boolean,
    @SerializedName("size") val size: String,
    @SerializedName("androidVersion") val androidVersion: String,
    @SerializedName("androidVersionText") val androidVersionText: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("genreId") val genreId: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("headerImage") val headerImage: String?,
    @SerializedName("screenshots") val screenshots: List<String>?,
    @SerializedName("video") val video: String?,
    @SerializedName("videoImage") val videoImage: String?,
    @SerializedName("contentRating") val contentRating: String?,
    @SerializedName("adSupported") val adSupported: Boolean,
    @SerializedName("released") val released: String?,
    @SerializedName("updated") val updated: Long,
    @SerializedName("version") val version: String?,
    @SerializedName("recentChanges") val recentChanges: String?,
    @SerializedName("comments") val comments: List<String>?,
    @SerializedName("url") val url: String,
    @SerializedName("similar") val similarApps: List<SimilarApp>?,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SimilarApp(
    @SerializedName("appId") val appId: String,
    @SerializedName("title") val title: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("developer") val developer: String,
    @SerializedName("score") val score: Double,
    @SerializedName("price") val price: String
)

data class Developer(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("website") val website: String?,
    @SerializedName("address") val address: String?
)
