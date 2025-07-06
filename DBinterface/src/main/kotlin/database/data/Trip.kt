package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String? = null,
    val userId: String,
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val isPublic: Boolean,
    val createdAt: String? = null
) : DatabaseClass

suspend fun postTrip(trip: Trip): Boolean =
    postToDatabase(trip, "trips", Trip.serializer())


