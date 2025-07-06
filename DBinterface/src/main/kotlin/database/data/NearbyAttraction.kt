package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NearbyAttraction(
    @SerialName("_id") val id: String? = null,
    val attractionId: String,
    val nearbyAttractionId: String,
    val distance: Double
) : DatabaseClass

fun postNearbyAttraction(nearbyAttraction: NearbyAttraction): Boolean =
    postToDatabase(nearbyAttraction, "nearby-attractions", NearbyAttraction.serializer())

