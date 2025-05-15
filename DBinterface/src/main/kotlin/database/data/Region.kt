package database.data

import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val id: String? = null,
    val name: String,
    val location: Coordinates
)


