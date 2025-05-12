package database

import org.bson.Document

fun testMongoConnection() {
    try {
        // pridobi kolekcijo users
        val collection = MongoDBClient.getCollection("users")

        collection.insertOne(Document("name", "Test User").append("email", "test@example.com"))


        val users = collection.find()
        for (user in users) {
            println(user.toJson())
        }

        println("MongoDB connection successful")
    } catch (e: Exception) {
        println("MongoDB connection failed: ${e.message}")
    }
}