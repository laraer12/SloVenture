package org.example

import it.skrape.fetcher.*
import it.skrape.core.*
import it.skrape.selects.attribute
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.section

data class AttractionDetails(
    val imageLinks: MutableList<String>,
    val descriptionParagraphs: MutableList<String>
)

fun main() {
    //preberemo in shranimo html željene strani
    val html = skrape(HttpFetcher) {
        request {
            url = "https://kamzavikend.si/ideja/mangart"
            method = Method.GET
        }

        response {
            responseBody
        }
    }

    val parsed = htmlDocument(html)

    val attractionDetails = AttractionDetails(
        imageLinks = mutableListOf(),
        descriptionParagraphs = mutableListOf()
    )

    //poiščemo ustrezen section
    parsed.section {
        withId = "main-content"

        findAll("img").forEach{ img->
            val classes = img.className.split(" ")

            //poiščemo slike, ki vsebujejo vse te razrede
            if(listOf(
                    "w-full",
                    "h-full",
                    "object-cover",
                    "rounded-xl",
                    "cursor-pointer"
                ).all { it in classes }){
                val src = img.attribute("src")
                attractionDetails.imageLinks.add(src)
            }
        }

        div {
            withId = "opis"

            findAll("p").forEach{p->
                attractionDetails.descriptionParagraphs.add(p.ownText)
            }
        }
    }

    for (link in attractionDetails.imageLinks){
        println("$link\n")
    }

    for (p in attractionDetails.descriptionParagraphs){
        println("$p\n")
    }

}