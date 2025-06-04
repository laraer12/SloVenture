package database

import database.data.*
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random


data class FakeDataOptions(
    val numUsers: Int = 10,
    val adminUserPercent: Int = 0,
    val numVisits: Int = 10,
    val numReviews: Int = 10,
    val minRating: Int = 1,
    val maxRating: Int = 5,
    val dateStart: LocalDate = LocalDate.now().minusYears(1),
    val dateEnd: LocalDate = LocalDate.now()
)


object FakeDataGenerator {
    private val faker = Faker()
    private val formatter = DateTimeFormatter.ISO_DATE

    fun generateFakeUsers(count: Int, options: FakeDataOptions): List<User> {
        return List(count) {
            val firstName = faker.name.firstName()
            val lastName = faker.name.lastName()
            val username = "${firstName.lowercase()}.${lastName.lowercase()}${Random.nextInt(10, 99)}"
            val isAdmin = Random.nextDouble() < options.adminUserPercent
            User(
                username = username,
                email = faker.internet.email(),
                password = faker.random.randomString(length = 10),
                profilePicture = "https://i.pravatar.cc/150?img=${Random.nextInt(1, 70)}",
                isAdmin = isAdmin,
                createdAt = formatter.format(randomDate(options.dateStart, options.dateEnd)),
                isFakeData = true
            )
        }
    }

    fun generateFakeUserVisits(
        count: Int,
        users: List<User>,
        attractions: List<AttractionMinimal>,
        dateRange: Pair<LocalDate, LocalDate>
    ): List<UserVisit> {
        return List(count) {
            UserVisit(
                userId = users.random().id ?: "",
                attractionId = attractions.random().id,
                visitDate = formatter.format(randomDate(dateRange.first, dateRange.second)),
                isFakeData = true
            )
        }
    }

    fun generateFakeReviews(
        count: Int,
        users: List<User>,
        attractions: List<AttractionMinimal>,
        dateRange: Pair<LocalDate, LocalDate>
    ): List<Review> {
        return List(count) {
            Review(
                userId = users.random().id ?: "",
                attractionId = attractions.random().id,
                rating = Random.nextInt(1, 6),
                ratingFamilyFriendly = Random.nextInt(1, 6),
                ratingElderlyFriendly = Random.nextInt(1, 6),
                ratingAccessible = Random.nextInt(1, 6),
                createdAt = formatter.format(randomDate(dateRange.first, dateRange.second)),
                isFakeData = true
            )
        }
    }

    fun generateAndPostAll(options: FakeDataOptions) = runBlocking {
        val attractions = getAllAttractions()
        if (attractions.isEmpty()) {
            println("No attractions found.")
            return@runBlocking
        }

        val users = generateFakeUsers(options.numUsers, options)
        users.forEach { postUser(it) }

        val updatedUsers = fetchUsersFromApi()

        val visits =
            generateFakeUserVisits(options.numVisits, updatedUsers, attractions, options.dateStart to options.dateEnd)
        visits.forEach { postUserVisit(it) }

        val reviews =
            generateFakeReviews(options.numReviews, updatedUsers, attractions, options.dateStart to options.dateEnd)
        reviews.forEach { postReview(it) }

        println("Fake data generation complete.")
    }

    private fun randomDate(start: LocalDate, end: LocalDate): LocalDate {
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end)
        if (daysBetween <= 0) return start
        return start.plusDays(Random.nextLong(0, daysBetween))
    }
}