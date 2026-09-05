package com.example.data.model

data class MediaItem(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "movies", // "movies", "series", "bollywood", "hindi-dubbed", "bongo"
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val streamUrl: String = "",
    val rating: String = "4.6",
    val year: String = "2024",
    val duration: String = "2h 15m",
    val genre: String = "Action",
    val label: String = "Free",
    val episodes: List<EpisodeItem> = emptyList(),
    val cast: List<String> = emptyList()
)

data class EpisodeItem(
    val id: String,
    val title: String,
    val episodeNumber: Int,
    val duration: String = "45m",
    val thumbnail: String = "",
    val streamUrl: String = ""
)

data class LiveChannel(
    val id: String,
    val name: String,
    val logoUrl: String = "",
    val group: String = "General",
    val streamUrl: String = ""
)

data class ContentSection(
    val title: String,
    val items: List<MediaItem>
)

data class SubscriptionPlan(
    val name: String,
    val price: String,
    val period: String,
    val isFeatured: Boolean = false,
    val features: List<String>
)
