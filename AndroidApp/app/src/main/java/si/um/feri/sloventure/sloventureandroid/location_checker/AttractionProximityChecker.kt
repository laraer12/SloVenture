package si.um.feri.sloventure.sloventureandroid.location_checker

import android.location.Location
import si.um.feri.sloventure.sloventureandroid.model.Attraction

class AttractionProximityChecker(
    private val attractions: List<Attraction>
) {
    fun findNearbyAttraction(
        location: Location,
        radiusMeters: Float = 50f
    ): Attraction? {
        attractions.forEach { attraction ->
            val result = FloatArray(1)

            Location.distanceBetween(
                location.latitude,
                location.longitude,
                attraction.location.lat,
                attraction.location.lon,
                result
            )
            // Timber.i("Distance to ${attraction.name}: ${result[0]} meters")
        }
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