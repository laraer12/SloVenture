import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.fetchWeatherData
import api.retrieveAllAttractions
import api.retrieveAttractions
import api.reverseGeocode
import database.data.*
import ui.MainScreen
import kotlinx.coroutines.runBlocking
import webScraper.fetchAttractionDetails
import kotlinx.coroutines.runBlocking

//@Preview


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainScreen()
    }
}
/*
fun main() = runBlocking {

    val attraction= getAttractionById("682c5aec18a16faef25c8b11")
    println(attraction.toString())


    // TESTNI PRIMERI ZA APIJE IN WEB SCRAPER
    /*
    try {

        println("\nScraping details for Mangart...")
        val details = fetchAttractionDetails("mangart")
        println("Images: ${details.imageLinks.size}")
        println("Descriptions: ${details.descriptionParagraphs.size}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    try {
        val lat = 46.55472
        val lon = 15.64667

        println("\nFetching weather for lat=$lat, lon=$lon")
        val weatherData = fetchWeatherData(lat, lon)
        println("Current: ${weatherData.currentWeather}")
        println("Forecast entries: ${weatherData.forecast.daily.toString()}")
    } catch (e: Exception) {
        println("Error during test: ${e.message}")
        e.printStackTrace()
    }

//cerkev
    val category = "kultura"
    val type = "cerkev"
    val num = 10
    val page = 1

    //val attractions = retrieveAttractions(category, type, num, page)
    val attractions= retrieveAllAttractions()


    if (attractions.isEmpty()) {
        println("No attractions retrieved.")
    }
    println("Retrieved ${attractions.size} attractions:")
    for (attraction in attractions) {
        println("- ${attraction.name} at ${attraction.location.lat}, ${attraction.location.lon}")
        println("      ${attraction.description}")

        if (attraction.images.isNotEmpty()) {
            println("Images:")
            for (img in attraction.images) {
                println("    • ${img.url} (source: ${img.source}, uploadedBy: ${img.uploadedBy})")
            }
        } else {
            println("No images available.")
        }

        val updatedAttraction = attraction.copy(regionId = "682731c683c43363632dd813")
        val savedAttractionId = postAttractionFromApi(updatedAttraction)


        if (savedAttractionId != null) {
            println("Saved attraction '${attraction.name}' to database.")

            for (image in attraction.images) {
                val imageWithCorrectId = image.copy(
                    attractionId = savedAttractionId,
                    uploadedBy = "682afdbd9ea2a20014bae933" //TODO ZACASNO SAMO DA LAHKO SHRANIM
                )
                val imageSaved = postAttractionImage(imageWithCorrectId)
                if (imageSaved) {
                    println("Saved image: ${image.url}")
                } else {
                    println("Failed to save image: ${image.url}")
                }
            }
        } else {
            println("Failed to save attraction '${attraction.name}'.")
        }
    }


    val coordinates = Coordinates(lat = 46.282617972686, lon = 13.862262386349)
    try {
        val address = reverseGeocode(coordinates)
        println("Resolved address:")
        println("Street: ${address.street}")
        println("City: ${address.city}")
        println("Postal Code: ${address.postalCode}")
        println("Country: ${address.country}")
    } catch (e: Exception) {
        println("Failed to get address: ${e.message}")
    }
<<<<<<< Updated upstream
*/
=======
    */

>>>>>>> Stashed changes



    /* TESTNI PRIMERI ZA SHRANJEVANJE
    val comment = Comment(
        userId = "682afdbd9ea2a20014bae933",
        text = "Hihi hecen komentar",
        attractionId = "682b17581320cdce0a71104b",
        createdAt = "2025-05-19T11:34:48.842+00:00"
    )
    postComment(comment)

    val accommodation = NearbyAccommodation(
        name = "Nearby Hotel",
        linkToBooking = "https://booking.com/hotel",
        attractionId = "682b17591320cdce0a71105d",
        distance = 0.5
    )
    postNearbyAccommodation(accommodation)


    val nearbyAttraction = NearbyAttraction(
        attractionId = "682b17581320cdce0a71104b",
        nearbyAttractionId = "682b17591320cdce0a71105d",
        distance = 1.2
    )
    postNearbyAttraction(nearbyAttraction)

    val region = Region(
        name = "Mountain Region",
        location = Coordinates(45.0, 19.0)
    )
    postRegion(region)

    val review = Review(
        userId = "682afdbd9ea2a20014bae933",
        attractionId = "682b17581320cdce0a71104b",
        rating = 4,
        ratingFamilyFriendly = 5,
        ratingElderlyFriendly = 4,
        ratingAccessible = 3,
        createdAt = "2025-05-19T12:00:00Z"
    )
    postReview(review)

    val trip = Trip(
        userId = "682afdbd9ea2a20014bae933",
        name = "My Trip",
        description = "Exploring Serbia",
        startDate = "2025-06-01",
        endDate = "2025-06-10",
        isPublic = true,
        createdAt = "2025-05-19T09:00:00Z"
    )
    postTrip(trip)

    val tripAttraction = TripAttraction(
        tripId = "682c123e1320cdce0a71105a",
        attractionId = "682b17581320cdce0a71104b",
        order = 1,
        description = "Visit early in the morning",
        plannedVisitTime = "2025-06-02T08:00:00Z"
    )
    postTripAttraction(tripAttraction)

    val user = User(
        username = "testuser",
        email = "test@example.com",
        password = "password123",
        profilePicture = "https://example.com/profile.jpg",
        role = "user",
        createdAt = "2025-05-19T08:00:00Z"
    )
    postUser(user)

    val userSaved = UserSaved(
        userId = "682afdbd9ea2a20014bae933",
        attractionId = "682b17581320cdce0a71104b"
    )
    postUserSaved(userSaved)

    val userViewHistory = UserViewHistory(
        userId = "682afdbd9ea2a20014bae933",
        attractionId = "682b17581320cdce0a71104b",
        viewedAt = "2025-05-18T18:30:00Z"
    )
    postUserViewHistory(userViewHistory)

    val userVisit = UserVisit(
        userId = "682afdbd9ea2a20014bae933",
        attractionId = "682b17581320cdce0a71104b",
        visitDate = "2025-05-15T14:00:00Z"
    )
    postUserVisit(userVisit)

    val weatherData = WeatherData(
        location = Coordinates(45.0, 19.0),
        attractionId = "682b17581320cdce0a71104b",
        currentWeather = Weather(
            date = "2025-05-19",
            temperature = 20.0,
            condition = "Sunny",
            maxTemperature = 23.0,
            minTemperature = 15.0,
            precipitationProbability = 10
        ),
        forecast = WeatherForecast(
            daily = listOf(
                Weather("2025-05-20", 21.0, "Cloudy", 24.0, 16.0, 20),
                Weather("2025-05-21", 19.0, "Rain", 22.0, 14.0, 80)
            )
        ),
        lastUpdated = "2025-05-19T11:00:00Z"
    )
    postWeatherData(weatherData)
    */
