package database

fun testMongoConnection() {
    try {
        // pridobi kolekcijo users
        val collection = MongoDBClient.getCollection("users")

        // preberi vse dokumente
        val documents = collection.find()

        // izpiši vse dokumente v konzolo
        for (doc in documents) {
            println(doc.toJson())
        }

        println("MongoDB connection successful")
    } catch (e: Exception) {
        println("MongoDB connection failed: ${e.message}")
    }
}