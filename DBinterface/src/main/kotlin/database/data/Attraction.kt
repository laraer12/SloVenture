package database.data

import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    val id: String? = null,
    val name: String,
    val regionId: String,
    val location: Coordinates,
    val address: Address,
    val description: String?,
    val classification: String,
    val locationType: String,
    val elevation: Double,
    val accessibilityOptions: String?,
    val ratingFamilyFriendly: Double,
    val ratingElderlyFriendly: Double,
    val ratingAccessible: Double,
    val rating: Double,
    val parkingInfo: ParkingInfo,
    val requiresReservation: Boolean,
    val openingHours: OpeningHours,
    val entryFee: Double,
    val hikingInfo: HikingInfo,
    val googleMapsLink: String,
    val createdAt: String? = null,
    val verified: Boolean
)

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
)

@Serializable
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
)

@Serializable
//se bo se spremenilo TODO
data class ParkingInfo(
    val hasParking: Boolean,
    val distanceToParkingMeters: Double?,
    val notes: String? = null
)

@Serializable
//se bo se spremenilo TODO
data class HikingInfo(
    val difficulty: String,
    val estimatedDurationMinutes: Int,
    val trailType: String
)

@Serializable
data class OpeningHours(
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String,
    val sunday: String
)