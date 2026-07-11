package com.watermelon.music.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val duration: String,
    val streamUrl: String? = null
)
