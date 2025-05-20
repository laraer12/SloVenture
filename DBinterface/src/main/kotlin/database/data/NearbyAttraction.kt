package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class NearbyAttraction(
    val id: String? = null,
    val attractionId: String,
    val nearbyAttractionId: String,
    val distance: Double
) : DatabaseClass

suspend fun postNearbyAttraction(nearbyAttraction: NearbyAttraction): Boolean =
    postToDatabase(nearbyAttraction, "nearby-attractions", NearbyAttraction.serializer())

