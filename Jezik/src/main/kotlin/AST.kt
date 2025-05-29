package org.example

import kotlin.math.*

operator fun Double.times(p: Point): Point = p * this

class Bezier(private val p0: Point, private val p1: Point, private val p2: Point, private val p3: Point) {

    private fun at(t: Double) =
        p0 * (1.0 - t).pow(3.0) + p1 * 3.0 * (1.0 - t).pow(2.0) * t + p2 * 3.0 * (1.0 - t) * t.pow(2.0) + p3 * t.pow(3.0)

    fun toPoints(segmentsCount: Int): List<Point> {
        val ps = mutableListOf<Point>()
        for (i in 0..segmentsCount) {
            val t = i / segmentsCount.toDouble()
            ps.add(at(t))
        }
        return ps
    }

    private fun approxLength(): Double {
        val midpoint = at(0.5)
        return p0.dist(midpoint) + midpoint.dist(p3)
    }

    fun resolutionToSegmentsCount(resolution: Double) =
        (resolution * approxLength()).coerceAtLeast(2.0).toInt()

    companion object {
        fun bend(t0: Point, t1: Point, relativeAngle: Double): Bezier {
            val relativeAngle = Math.toRadians(relativeAngle)
            val oppositeRelativeAngle = PI - relativeAngle

            val angle = t0.angle(t1)
            val dist = t0.dist(t1)
            val constant = (4 / 3) * tan(PI / 8)

            val c0 = t0.offset(constant * dist, angle + relativeAngle)
            val c1 = t1.offset(constant * dist, angle + oppositeRelativeAngle)

            return Bezier(t0, c0, c1, t1)
        }
    }
}

interface AST {
    fun toGeoJson(): String
}

class Program(
    val regions: MutableList<Region> = mutableListOf(),
    val paths: MutableList<Path> = mutableListOf(),
    val vars: MutableList<Var> = mutableListOf(),
    val pvars: MutableList<Pvar> = mutableListOf()
) : AST {
    override fun toGeoJson(): String {
        val features = mutableListOf<String>()

        if(regions.isNotEmpty()) {
            features.addAll(regions.map { it.toGeoJson() })
        }

        if(paths.isNotEmpty()) {
            features.addAll(paths.map { it.toGeoJson() })
        }

        val allGeoJson = features.joinToString(", ")
        return """{
            "type": "FeatureCollection",
            "features": [
                $allGeoJson
            ]
        }""".trimMargin()
    }
}

class Region(
    private val name: String,
    private val area: Poliline,
    val attractions: List<Attraction>,
    private val nearby: List<Nearby>
) : AST {
    override fun toGeoJson(): String {
        val attractionsGeoJson = attractions.joinToString(", ") { it.toGeoJson() }
        val nearbyGeoJson = nearby.joinToString(", ") { it.toGeoJson() }

        val areaFeature = """{
            "type": "Feature",
            "geometry": ${area.toGeoJson()},
            "properties": { "name": "$name" }
        }""".trimMargin()

        return listOf(areaFeature, attractionsGeoJson, nearbyGeoJson)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

class Poliline(
    private val points: List<Point>
) : Instruction, AST {
    override fun toGeoJson(): String {
        val coords = points.joinToString(", ") { "[${it.long}, ${it.lat}]" }
        return """{"type": "LineString", "coordinates": [$coords]}"""
    }
}

class Point(
    val long: Double,
    val lat: Double
) : AST {
    override fun toGeoJson(): String {
        return """{
        "type": "Point", 
        "coordinates": [$long, $lat]
    }"""
    }

    //dodane funkcije in prekrivanja operatorjev zaradi bezier
    operator fun plus(other: Point): Point = Point(this.long + other.long, this.lat + other.lat)

    operator fun times(scalar: Double): Point = Point(this.long * scalar, this.lat * scalar)

    fun dist(other: Point): Double {
        val dx = this.long - other.long
        val dy = this.lat - other.lat
        return sqrt(dx * dx + dy * dy)
    }

    fun angle(other: Point): Double {
        return atan2(other.lat - this.lat, other.long - this.long)
    }

    fun offset(distance: Double, angle: Double): Point {
        return Point(long + distance * cos(angle),lat + distance * sin(angle))
    }

    fun toMeters(): Pair<Double, Double> {
        val r = 6371.0  // radij zemlje v metrih
        val x = Math.toRadians(long) * r * cos(Math.toRadians(lat))
        val y = Math.toRadians(lat) * r
        return x to y
    }

    fun approxDist(other: Point): Double {
        val (x1, y1) = this.toMeters()
        val (x2, y2) = other.toMeters()
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }
}

abstract class Attraction(
    val name: String,
    val point: Point
) : AST {
    override fun toGeoJson(): String {
        return ""
    }

    //GeoJsonu od znamenitosti doda še atribut near
    open fun toNearbyGeoJson(nearPoint: String): String = ""
}

class Hill(
    name: String,
    point: Point,
    private val area: Poliline
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Hill area" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Hill point" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Hill area", "near": "$nearPoint" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Hill point", "near": "$nearPoint" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Cabin(
    name: String,
    point: Point,
    private val area: Box
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Cabin area" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Cabin point" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Cabin area", "near": "$nearPoint" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Cabin point", "near": "$nearPoint" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Church(
    name: String,
    point: Point,
    private val area: Box
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Church area" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Church point" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Church area", "near": "$nearPoint" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Church point", "near": "$nearPoint" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Mountain(
    name: String,
    point: Point,
    private val area: Poliline
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Mountain area" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Mountain point" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Mountain area", "near": "$nearPoint" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Mountain point", "near": "$nearPoint" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Other(
    name: String,
    point: Point
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        return """{
            "type": "Feature",
            "geometry": ${point.toGeoJson()},
            "properties": { "name": "$name", "type": "Other point"
            }
        }"""
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        return """{
            "type": "Feature",
            "geometry": ${point.toGeoJson()},
            "properties": { "name": "$name", "type": "Other point", "near": "$nearPoint"
            }
        }"""
    }
}

class Castle(
    name: String,
    point: Point,
    private val area: Box
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Castle area" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Castle point" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val areaFeature = """{
        "type": "Feature",
        "geometry": ${area.toGeoJson()},
        "properties": { "name": "$name", "type": "Castle area", "near": "$nearPoint" }
    }"""

        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Castle point", "near": "$nearPoint" }
    }"""

        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Lake(
    name: String,
    point: Point,
    private val area: Circle
) : Attraction(name, point) {
    override fun toGeoJson(): String {
        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Lake point" }
    }"""
        val areaFeature = """{
            "type": "Feature",
            "geometry": ${area.toGeoJson()},
            "properties": {
                "name": "$name",
                "type": "Lake area"
            }
        }""".trimMargin()
        return listOf(areaFeature, pointFeature).joinToString(", ")
    }

    override fun toNearbyGeoJson(nearPoint: String): String {
        val pointFeature = """{
        "type": "Feature", "geometry": ${point.toGeoJson()},
        "properties": { "name": "$name", "type": "Lake point", "near": "$nearPoint" }
    }"""
        val areaFeature = """{
            "type": "Feature",
            "geometry": ${area.toGeoJson()},
            "properties": {
                "name": "$name",
                "type": "Lake area",
                "near": "$nearPoint"
            }
        }""".trimMargin()
        return listOf(areaFeature, pointFeature).joinToString(", ")
    }
}

class Circle(
    private val point: Point,
    private val r: Double //radij v km
) : AST {
    override fun toGeoJson(): String {
        val R = 6371.0  // zemljin radij v km
        val c = r / R   // kotni radij

        val latRad = Math.toRadians(point.lat)
        val lonRad = Math.toRadians(point.long)

        val coordinates = mutableListOf<String>()

        for (i in 0..360 step 10) {
            val beta = Math.toRadians(i.toDouble())

            val lat_ = asin(sin(latRad) * cos(c) + cos(latRad) * sin(c) * cos(beta))
            val lon_ = lonRad + atan2(
                sin(beta) * sin(c) * cos(latRad), cos(c) - sin(latRad) * sin(lat_)
            )

            val latDeg = Math.toDegrees(lat_)
            val lonDeg = Math.toDegrees(lon_)
            coordinates.add("[$lonDeg, $latDeg]")
        }

        // zapremo krog (dodamo prvo točko na konec)
        coordinates.add(coordinates[0])

        return """{
             "type": "Polygon",
             "coordinates": [ 
                [ ${coordinates.joinToString(", ")} ]
             ]
        }"""
    }
}

class Path(
    private val instructions: List<Instruction>
) : AST {
    override fun toGeoJson(): String {
        return instructions.joinToString(", ") { instruction ->
            """{
                "type": "Feature",
                "geometry": ${instruction.toGeoJson()},
                "properties": {}
            }""".trimMargin()
        }
    }
}

class Box(
    private val topLeft: Point,
    private val bottomRight: Point
) : AST {
    override fun toGeoJson(): String {
        val topRight = Point(bottomRight.long, topLeft.lat)
        val bottomLeft = Point(topLeft.long, bottomRight.lat)

        val coordinates =
            listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft).joinToString(", ") { "[${it.long}, ${it.lat}]" }

        return """{
            "type": "Polygon",
            "coordinates": [
                    [ $coordinates]
                ]
        }"""
    }
}

interface Instruction : AST

class Line(
    private val start: Point,
    private val end: Point
) : Instruction, AST {
    override fun toGeoJson(): String {
        return """{
            "type": "LineString", 
            "coordinates": [
                [${start.long}, ${start.lat}], 
                [${end.long}, ${end.lat}]
            ]
        }"""
    }
}

class Bend(
    val start: Point,
    val end: Point,
    val angle: Double
) : Instruction, AST {
    override fun toGeoJson(): String {
        val bezier = Bezier.bend(start, end, angle)
        val points = bezier.toPoints(20)
        val coordinates = points.joinToString(", ") { "[${it.long}, ${it.lat}]" }
        return """{
            "type": "LineString", 
            "coordinates": [$coordinates]
        }"""
    }
}

class Polispline(
    private val bends: List<Bend>
) : Instruction, AST {

    init{
        require(validateConnections())
    }
    override fun toGeoJson(): String {
        val coordinates = bends.flatMap { bend ->
            val bezier = Bezier.bend(bend.start, bend.end, bend.angle)
            bezier.toPoints(10)
        }.joinToString(", ") { "[${it.long}, ${it.lat}]" }

        return """{
            "type": "LineString",
            "coordinates": [$coordinates]
        }"""
    }

    //preveri ali je neprekinjena črta
    private fun validateConnections(): Boolean{
        for (i in 0 until bends.size-1){
            val currentEnd = Pair( bends[i].end.long, bends[i].end.lat)
            val nextStart = Pair( bends[i+1].start.long, bends[i+1].start.lat)
            if(currentEnd != nextStart){
                println("Bend $i ends at $currentEnd, but next bend starts at $nextStart!")
                return false
            }
        }
        return true
    }
}

class Var(
    val name: String,
    val value: Double
)

class Pvar(
    val name: String,
    val value: Point
)

class Nearby(
    private val p: Point,
    private val r: Double,
    private val program: Program
) : AST {
    private fun allAttractions(): List<Attraction> = program.regions.flatMap { it.attractions }

    override fun toGeoJson(): String {
        val circle = Circle(p, r)
        val inside = allAttractions().filter {
            it.point.approxDist(p) <= r
        }

        val circleGeoJson = """{
            "type": "Feature",
            "geometry": ${circle.toGeoJson()},
            "properties": {
            "near point": "(${p.long}, ${p.lat})",
            "radius": "$r"
            }
        }""".trimMargin()

        //doda spremenjene znamenitosti z atributom near
        val insideGeoJson = inside.joinToString(", ") { it.toNearbyGeoJson("(${p.long}, ${p.lat})")
        }
        return listOf(circleGeoJson, insideGeoJson).joinToString(", ")
    }
}