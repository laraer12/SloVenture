package si.um.feri.sloventure.sloventureandroid.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    @SerialName("_id") val id: String,
    val name: String,
    val location: Coordinates,
)

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
)