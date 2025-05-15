package database.data

import kotlinx.serialization.Serializable

@Serializable
data class TripAttraction(
    val id: String? = null,
    val tripId: String,
    val attractionId: String,
    val order: Int,
    val description: String,
    val plannedVisitTime: String? = null
)
