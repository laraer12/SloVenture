package database.data
import kotlinx.serialization.Serializable

@Serializable
data class AttractionImage(
    val id: String? = null,
    val attractionId: String,
    val url: String,
    val source: String,
    val uploadedBy: String,
    val createdAt: String? = null
)
