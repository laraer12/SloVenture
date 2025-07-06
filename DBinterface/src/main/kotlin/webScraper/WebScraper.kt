package webScraper

import it.skrape.fetcher.*
import it.skrape.core.*
import it.skrape.selects.html5.div
import it.skrape.selects.html5.section
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class AttractionDetails(
    val imageLinks: List<String>,
    val descriptionParagraphs: List<String>
)

suspend fun fetchAttractionDetails(attractionS: String):AttractionDetails{
    //preberemo in shranimo html željene strani
    val html = skrape(HttpFetcher) {
        request {
            url = "https://kamzavikend.si/ideja/$attractionS"
            method = Method.GET
        }
        response { responseBody }
    }
    val parsed = htmlDocument(html)

    val images = mutableListOf<String>()
    val paragraphs = mutableListOf<String>()

    //poiščemo ustrezen section
    parsed.section {
        withId = "main-content"

        //poiščemo slike, ki vsebujejo vse te razrede
        findAll("img").forEach { img ->
            val classes = img.className.split(" ")
            if (listOf(
                    "w-full", "h-full", "object-cover", "rounded-xl", "cursor-pointer"
                ).all { it in classes }
            ) {
                val src = img.attribute("src")
                images.add(src)
            }
        }

        div {
            withId = "opis"
            findAll("p").forEach { p -> paragraphs.add(p.ownText) }
        }
    }
    return AttractionDetails(images, paragraphs)
}