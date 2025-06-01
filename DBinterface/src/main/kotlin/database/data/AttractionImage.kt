package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class AttractionImage(
    val id: String? = null,
    val attractionId: String,
    val url: String,
    val source: String,
    val uploadedBy: String?,
    val createdAt: String? = null
) : DatabaseClass

suspend fun postAttractionImage(attractionImage: AttractionImage): Boolean =
    postToDatabase(attractionImage, "attraction-images", AttractionImage.serializer())

