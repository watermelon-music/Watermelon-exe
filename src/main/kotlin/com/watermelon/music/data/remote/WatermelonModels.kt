package com.watermelon.music.data.remote

import com.google.gson.annotations.SerializedName

data class WatermelonSearchResult(
    val value: List<WatermelonSong>,
    val Count: Int
)

data class WatermelonSong(
    val id: String,
    val title: String,
    val duration: String?,
    val duration_string: String?,
    val uploader: String?,
    val channel: String?,
    val thumbnail: String?
)

fun WatermelonSong.toSong() = com.watermelon.music.domain.model.Song(
    id = this.id,
    title = this.title ?: "",
    artist = this.uploader ?: this.channel ?: "Unknown Artist",
    thumbnail = this.thumbnail ?: "https://i.ytimg.com/vi/${this.id}/hqdefault.jpg",
    duration = this.duration_string ?: this.duration?.toString() ?: "0:00"
)
