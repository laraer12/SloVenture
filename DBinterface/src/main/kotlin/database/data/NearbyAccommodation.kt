package database.data

import kotlinx.serialization.Serializable

@Serializable
data class NearbyAccommodation(
    val id: String? = null,
    val name: String,
    val linkToBooking: String,
    val attractionId: String,
    val distance: Double
)
