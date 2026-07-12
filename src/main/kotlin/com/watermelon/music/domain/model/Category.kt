package com.watermelon.music.domain.model

data class Category(
    val id: String,
    val title: String,
    val query: String
)

val HOME_CATEGORIES = listOf(
    Category("hollywood", "Hollywood", "english trending songs"),
    Category("bollywood", "Bollywood", "bollywood trending songs"),
    Category("pop", "Pop", "pop music trending"),
    Category("rock", "Rock", "rock music"),
    Category("jazz", "Jazz & Blues", "jazz music"),
    Category("classical", "Classical", "classical music"),
    Category("hiphop", "Hip-Hop", "hip hop music"),
    Category("electronic", "Electronic", "electronic music")
)
