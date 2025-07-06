package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class TripAttraction(
    val id: String? = null,
    val tripId: String,
    val attractionId: String,
    val order: Int,
    val description: String,
    val plannedVisitTime: String? = null
) : DatabaseClass

suspend fun postTripAttraction(tripAttraction: TripAttraction): Boolean =
    postToDatabase(tripAttraction, "trip-attractions", TripAttraction.serializer())
