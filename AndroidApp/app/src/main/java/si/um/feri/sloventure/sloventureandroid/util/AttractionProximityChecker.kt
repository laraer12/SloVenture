package si.um.feri.sloventure.sloventureandroid.util

import android.location.Location
import si.um.feri.sloventure.sloventureandroid.model.Attraction

class AttractionProximityChecker(
    private val attractions: List<Attraction>
) {
    fun findNearbyAttraction(
        location: Location,
        radiusMeters: Float = 50f
    ): Attraction? {
        return attractions.firstOrNull { attraction ->
            val result = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                attraction.location.lat,
                attraction.location.lon,
                result
            )
            result[0] <= radiusMeters
        }
    }
}