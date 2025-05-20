package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val id: String? = null,
    val name: String,
    val location: List<Coordinates>
) : DatabaseClass

suspend fun postRegion(region: Region): Boolean =
    postToDatabase(region, "regions", Region.serializer())


