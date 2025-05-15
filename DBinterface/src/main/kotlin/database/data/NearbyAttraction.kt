package database.data

import kotlinx.serialization.Serializable

@Serializable
data class NearbyAttraction(
    val id: String? = null,
    val attractionId: String,
    val nearbyAttractionId: String,
    val distance: Double
)