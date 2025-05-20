package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class NearbyAccommodation(
    val id: String? = null,
    val name: String,
    val linkToBooking: String,
    val attractionId: String,
    val distance: Double
) : DatabaseClass

suspend fun postNearbyAccommodation(nearbyAccommodation: NearbyAccommodation): Boolean =
    postToDatabase(nearbyAccommodation, "nearby-accommodations", NearbyAccommodation.serializer())
