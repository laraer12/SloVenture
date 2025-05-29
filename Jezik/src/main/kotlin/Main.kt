package org.example

import java.io.File
import java.io.InputStream

const val ERROR_STATE = 0
const val EOF_SYMBOL = -1
const val SKIP_SYMBOL = 0
const val REGION_SYMBOL = 1
const val PATH_SYMBOL = 2
const val HILL_SYMBOL = 3
const val CABIN_SYMBOL = 4
const val CHURCH_SYMBOL = 5
const val MOUNTAIN_SYMBOL = 6
const val CASTLE_SYMBOL = 7
const val LAKE_SYMBOL = 8
const val OTHER_SYMBOL = 9
const val PVAR_SYMBOL = 10
const val NVAR_SYMBOL = 11
const val POINT_SYMBOL = 12
const val BOX_SYMBOL = 13
const val CIRCLE_SYMBOL = 14
const val NEARBY_SYMBOL = 15
const val FIRSTC_SYMBOL = 16
const val SECONDC_SYMBOL = 17
const val STRING_LITERAL_SYMBOL = 18
const val NUM_SYMBOL = 19
const val LBRACE_SYMBOL = 20
const val RBRACE_SYMBOL = 21
const val LPAREN_SYMBOL = 22
const val RPAREN_SYMBOL = 23
const val COMMA_SYMBOL = 24
const val PLUS_SYMBOL = 25
const val MINUS_SYMBOL = 26
const val TIMES_SYMBOL = 27
const val DIVIDE_SYMBOL = 28
const val BEND_SYMBOL = 29
const val LINE_SYMBOL = 30
const val POLILINE_SYMBOL = 31
const val POLISPLINE_SYMBOL = 32
const val VAR_SYMBOL = 33
const val NEWLINE = 34
const val ASSIGN_SYMBOL = 35
const val PVAR_DEC_SYMBOL = 36
const val NVAR_DEC_SYMBOL = 37

const val EOF = -1

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object ForForeachFFFAutomaton : DFA {
    override val states = (1..19).toSet()
    override val alphabet = 0..255
    override val startState = 1
    override val finalStates = setOf(2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19)

    private val numberOfStates = states.max() + 1
    private val numberOfCodes = alphabet.max() + 1
    private val transitions = Array(numberOfStates) { IntArray(numberOfCodes) }
    private val values = Array(numberOfStates) { SKIP_SYMBOL }

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setSymbol(state: Int, symbol: Int) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }

    init {
        //PVAR
        for (c in 'A'..'Z') setTransition(1, c, 2)
        for (c in 'A'..'Z') setTransition(2, c, 2)
        setSymbol(2, PVAR_SYMBOL)

        //VAR IN RESERVED WORDS
        for (c in 'a'..'z') setTransition(1, c, 3)
        for (c in 'a'..'z') setTransition(2, c, 3)
        for (c in '0'..'9') setTransition(2, c, 3)
        for (c in 'A'..'Z') setTransition(3, c, 3)
        for (c in 'a'..'z') setTransition(3, c, 3)
        for (c in 'A'..'Z') setTransition(3, c, 3)


        //NAMES - STRING LITERAL
        setTransition(1, '"', 4)
        for (c in 'A'..'Z') setTransition(4, c, 4)
        for (c in 'a'..'z') setTransition(4, c, 4)
        setTransition(4, ' ', 4)
        setTransition(4, '"', 5)
        setSymbol(5, STRING_LITERAL_SYMBOL)

        //{
        setTransition(1, '{', 6)
        setSymbol(6, LBRACE_SYMBOL)

        //}
        setTransition(1, '}', 7)
        setSymbol(7, RBRACE_SYMBOL)

        //)
        setTransition(1, ')', 8)
        setSymbol(8, RPAREN_SYMBOL)

        //,
        setTransition(1, ',', 9)
        setSymbol(9, COMMA_SYMBOL)

        //+
        setTransition(1, '+', 10)
        setSymbol(10, PLUS_SYMBOL)

        //-
        setTransition(1, '-', 11)
        setSymbol(11, MINUS_SYMBOL)

        //*
        setTransition(1, '*', 12)
        setSymbol(12, TIMES_SYMBOL)

        // /
        setTransition(1, '/', 13)
        setSymbol(13, DIVIDE_SYMBOL)

        //NUMBERS
        for (c in '0'..'9') setTransition(1, c, 14)
        for (c in '0'..'9') setTransition(14, c, 14)
        setTransition(14, '.', 15)
        for (c in '0'..'9') setTransition(15, c, 15)
        setSymbol(15, NUM_SYMBOL)

        setTransition(1, EOF, 16)
        setSymbol(16, EOF_SYMBOL)

        //SPACE
        setTransition(1, ' ', 17)
        setTransition(1, '\t', 17)
        setTransition(1, '\n', 17)
        setTransition(1, '\r', 17)
        setSymbol(17, SKIP_SYMBOL)

        //(
        setTransition(1, '(', 18)
        setSymbol(18, LPAREN_SYMBOL)

        //=
        setTransition(1, '=', 19)
        setSymbol(19, ASSIGN_SYMBOL)
    }
}

data class Token(val symbol: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break // Longest match

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code

        if (automaton.finalStates.contains(state)) {
            val lexeme = String(buffer.toCharArray())
            val symbol = if (state == 3) reservedWord(lexeme) else automaton.symbol(state)
            return if (symbol == SKIP_SYMBOL) {
                getToken()
            } else {
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }
}

fun reservedWord(lexeme: String): Int {
    return when (lexeme) {
        "Region" -> REGION_SYMBOL
        "Hill" -> HILL_SYMBOL
        "Cabin" -> CABIN_SYMBOL
        "Church" -> CHURCH_SYMBOL
        "circle" -> CIRCLE_SYMBOL
        "Castle" -> CASTLE_SYMBOL
        "pvar" -> PVAR_DEC_SYMBOL
        "var" -> NVAR_DEC_SYMBOL
        "box" -> BOX_SYMBOL
        "firstC" -> FIRSTC_SYMBOL
        "secondC" -> SECONDC_SYMBOL
        "bend" -> BEND_SYMBOL
        "line" -> LINE_SYMBOL
        "poliline" -> POLILINE_SYMBOL
        "polispline" -> POLISPLINE_SYMBOL
        "Lake" -> LAKE_SYMBOL
        "Mountain" -> MOUNTAIN_SYMBOL
        "nearby" -> NEARBY_SYMBOL
        "Other" -> OTHER_SYMBOL
        "Path" -> PATH_SYMBOL
        "point" -> POINT_SYMBOL
        else -> VAR_SYMBOL
    }
}

fun name(symbol: Int) =
    when (symbol) {
        REGION_SYMBOL -> "REGION"
        HILL_SYMBOL -> "HILL"
        CABIN_SYMBOL -> "CABIN"
        CHURCH_SYMBOL -> "CHURCH"
        CIRCLE_SYMBOL -> "CIRCLE"
        CASTLE_SYMBOL -> "CASTLE"
        PVAR_SYMBOL -> "PVAR"
        NVAR_SYMBOL -> "VAR"
        BOX_SYMBOL -> "BOX"
        LPAREN_SYMBOL -> "LPAREN"
        FIRSTC_SYMBOL -> "FIRSTC"
        SECONDC_SYMBOL -> "SECONDC"
        BEND_SYMBOL -> "BEND("
        LINE_SYMBOL -> "LINE("
        POLILINE_SYMBOL -> "POLILINE"
        POLISPLINE_SYMBOL -> "POLISPLINE"
        LAKE_SYMBOL -> "LAKE"
        MOUNTAIN_SYMBOL -> "MOUNTAIN"
        NEARBY_SYMBOL -> "NEARBY"
        OTHER_SYMBOL -> "OTHER"
        PATH_SYMBOL -> "PATH"
        POINT_SYMBOL -> "POINT"
        PLUS_SYMBOL -> "PLUS"
        MINUS_SYMBOL -> "MINUS"
        TIMES_SYMBOL -> "TIMES"
        DIVIDE_SYMBOL -> "DIVIDE"
        VAR_SYMBOL -> "VAR"
        COMMA_SYMBOL -> "COMMA"
        LBRACE_SYMBOL -> "LBRACE"
        RBRACE_SYMBOL -> "RBRACE"
        RPAREN_SYMBOL -> "RPAREN"
        STRING_LITERAL_SYMBOL -> "STRING"
        NUM_SYMBOL -> "NUMBER"
        ASSIGN_SYMBOL -> "ASSIGN"
        NVAR_DEC_SYMBOL -> "DEC_VAR"
        PVAR_DEC_SYMBOL -> "DEC_PVAR"
        else -> throw Error("Invalid symbol")
    }

class Parser(private val scanner: Scanner) {
    private var token: Token = scanner.getToken()
    private val program = Program()

    private fun accept(symbol: Int): Boolean {
        if (token.symbol == symbol) {
            token = scanner.getToken()
            return true
        }
        return false
    }

    private fun expect(symbol: Int): Boolean {
        if (!accept(symbol)) {
            println("Syntax error at ${token.startRow}:${token.startColumn}, expected ${name(symbol)}, got ${name(token.symbol)}")
            return false
        }
        return true
    }

    fun parse(): String? {
        val success = program()

        if(!success || token.symbol != EOF_SYMBOL){
            println("Parsing failed or unexpected input at ${token.startRow}:${token.startColumn}")
            return null
        }

        return program.toGeoJson()
    }


    private fun program(): Boolean {
        return statements()
    }

    private fun statements(): Boolean {
        while (true) {
            val result = statement()
            if (!result) {
                return false
            }
            if (token.symbol == RBRACE_SYMBOL || token.symbol == EOF_SYMBOL) {
                break
            }
        }
        return true
    }

    private fun statement(): Boolean {
        val region = region()
        if(region != null){
            program.regions.add(region)
            return true
        }
        if (declaration()) return true

        val path = path()
        if(path != null){
            program.paths.add(path)
            return true
        }
        return false
    }

    private fun region(): Region? {
        if (!accept(REGION_SYMBOL)) return null
        val name = token.lexeme.trim('"')
        if (!expect(STRING_LITERAL_SYMBOL)) return null
        if (!expect(LBRACE_SYMBOL)) return null
        val area = regionArea() ?: return null
        val attractions = attractions() ?: return null
        if (!expect(RBRACE_SYMBOL)) return null
        return Region(name, area, attractions.first, attractions.second)
    }

    private fun regionArea(): Poliline? {
        return poliline()
    }

    private fun attractions(): Pair<List<Attraction>, List<Nearby>>? {
        val attractions = mutableListOf<Attraction>()
        val nearbyList = mutableListOf<Nearby>()
        while (token.symbol !in listOf(RBRACE_SYMBOL, EOF_SYMBOL)) {
            if(token.symbol == NEARBY_SYMBOL){
                val nearby = nearby() ?: return null
                nearbyList.add(nearby)
                continue
            }
            else if(token.symbol == NVAR_DEC_SYMBOL){
                if(!varDeclaration()) return null
                continue
            }else if (token.symbol == PVAR_DEC_SYMBOL){
                if(!pvarDeclaration())  return null
                continue
            }
            val attraction = attraction() ?: return null
            attractions.add(attraction)
        }
        return Pair(attractions, nearbyList)
    }

    private fun attraction(): Attraction? {
        return hill()
            ?: cabin()
            ?: church()
            ?: mountain()
            ?: other()
            ?: castle()
            ?: lake()
    }

    private fun hill(): Hill? {
        val (name, point) = simpleAttraction(HILL_SYMBOL)?:return null
        val area = poliline()?: return null
        return Hill(name, point, area)
    }

    private fun cabin(): Cabin? {
        val (name, point) = simpleAttraction(CABIN_SYMBOL)?:return null
        val area = box()?:return null
        return Cabin(name, point, area)
    }

    private fun church(): Church? {
        val (name, point) = simpleAttraction(CHURCH_SYMBOL)?:return null
        val area = box()?: return null
        return Church(name, point, area)
    }

    private fun mountain(): Mountain? {
        val (name, point) = simpleAttraction(MOUNTAIN_SYMBOL)?:return null
        val area = poliline()?: return null
        return Mountain(name, point, area)
    }

    private fun other(): Other? {
        val (name, point) = simpleAttraction(OTHER_SYMBOL)?:return null
        return Other(name, point)
    }

    private fun castle(): Castle? {
        val (name, point) = simpleAttraction(CASTLE_SYMBOL)?:return null
        val area = box()?: return null
        return Castle(name, point, area)
    }

    private fun lake(): Lake? {
        val (name, point) = simpleAttraction(LAKE_SYMBOL)?:return null
        val area = circle()?: return null
        return Lake(name, point, area)
    }

    private fun nearby(): Nearby? {
        if (!accept(NEARBY_SYMBOL)) return null
        if (!expect(LPAREN_SYMBOL)) return null
        val p = point()?: return null
        if (!expect(COMMA_SYMBOL)) return null
        val r = expr()?: return null
        if(!expect(RPAREN_SYMBOL)) return null
        return Nearby(p, r, program)
    }

    private fun declaration(): Boolean {
        return pvarDeclaration() || varDeclaration()
    }

    private fun pvarDeclaration(): Boolean {
        if (!accept(PVAR_DEC_SYMBOL)) return false
        val name = token.lexeme
        if (!expect(PVAR_SYMBOL)) return false
        if (!expect(ASSIGN_SYMBOL)) return false
        val value = point()?:return false
        val newPvar = Pvar(name, value)
        program.pvars.add(newPvar)
        return true
    }

    private fun varDeclaration(): Boolean {
        if (!accept(NVAR_DEC_SYMBOL)) return false
        val name = token.lexeme
        if (!expect(VAR_SYMBOL)) return false
        if (!expect(ASSIGN_SYMBOL)) return false
        val value = expr()?:return false
        val newVar = Var(name, value)
        program.vars.add(newVar)
        return true
    }

    private fun expr(): Double? = additive()

    private fun additive(): Double? {
        var result = multiplicative() ?: return null
        while (token.symbol == PLUS_SYMBOL || token.symbol == MINUS_SYMBOL) {
            val op = token.symbol
            token = scanner.getToken()
            val right = multiplicative() ?: return null
            result = if (op == PLUS_SYMBOL) result + right else result - right
        }
        return result
    }

    private fun multiplicative(): Double? {
        var result = unary() ?: return null
        while (token.symbol == TIMES_SYMBOL || token.symbol == DIVIDE_SYMBOL) {
            val op = token.symbol
            token = scanner.getToken()
            val right = unary() ?: return null
            result = if (op == TIMES_SYMBOL) result * right else result / right
        }
        return result
    }

    private fun unary(): Double? {
        return when (token.symbol) {
            PLUS_SYMBOL -> {
                token = scanner.getToken()
                unary()
            }
            MINUS_SYMBOL -> {
                token = scanner.getToken()
                unary()?.let { -it }
            }
            else -> term()
        }
    }

    private fun term(): Double? {
        return when (token.symbol) {
            FIRSTC_SYMBOL -> {
                token = scanner.getToken()
                if (!expect(LPAREN_SYMBOL)) return null
                val p = point()?: return null
                if (!expect(RPAREN_SYMBOL)) return null
                p.long
            }

            SECONDC_SYMBOL -> {
                token = scanner.getToken()
                if (!expect(LPAREN_SYMBOL)) return null
                val p = point()?: return null
                if (!expect(RPAREN_SYMBOL)) return null
                p.lat
            }

            NUM_SYMBOL -> {
                val raw = token.lexeme.trim('"')
                val number = raw.toDoubleOrNull()
                token = scanner.getToken()
                number
            }

            VAR_SYMBOL -> {
                val name = token.lexeme
                token = scanner.getToken()
                val variable = program.vars.find { it.name == name }
                if (variable == null) {
                    println("Undefined variable: $name")
                    return null
                }
                variable.value
            }

            else -> null
        }
    }

    private fun point(): Point? {
        val pvar = token.lexeme
        return if (accept(POINT_SYMBOL)) {
            if (!expect(LPAREN_SYMBOL)) return null
            val x = expr() ?: return null
            if (!expect(COMMA_SYMBOL)) return null
            val y = expr() ?: return null
            if (!expect(RPAREN_SYMBOL)) return null
            Point(x, y)
        } else if (accept(PVAR_SYMBOL)) {
            program.pvars.find { it.name == pvar }?.value
        } else {
            null
        }
    }

    private fun box(): Box? {
        if (!accept(BOX_SYMBOL))return null
        if (!expect(LPAREN_SYMBOL))return null

        val topLeft = point()?: return null

        if (!expect(COMMA_SYMBOL)) return null

        val bottomRight = point()?: return null

        if (!expect(RPAREN_SYMBOL)) return null
        return Box(topLeft, bottomRight)
    }

    private fun circle(): Circle? {
        if (!accept(CIRCLE_SYMBOL))return null
        if (!expect(LPAREN_SYMBOL)) return null

        val p = point()?:return null

        if (!expect(COMMA_SYMBOL)) return null
        val r = expr() ?: return null

        if (!expect(RPAREN_SYMBOL)) return null
        return Circle(p, r)
    }

    private fun path(): Path? {
        if (!accept(PATH_SYMBOL)) return null
        if (!expect(LBRACE_SYMBOL)) return null
        val instructions = instructions() ?: return null
        if (!expect(RBRACE_SYMBOL)) return null
        return Path(instructions)
    }

    private fun instructions(): List<Instruction>? {
        val instructions = mutableListOf<Instruction>()
        while (true) {
            if(token.symbol == RBRACE_SYMBOL) break
            val instruction = instruction() ?: return null
            instructions.add(instruction)
        }
        return instructions
    }

    private fun instruction(): Instruction? {
        return poliline() ?: polispline() ?: line() ?: bend()
    }

    private fun poliline(): Poliline? {
        val points = mutableListOf<Point>()
        if (!accept(POLILINE_SYMBOL)) return null
        if (!expect(LPAREN_SYMBOL)) return null
        val firstPoint = point() ?: return null
        points.add(firstPoint)
        while (accept(COMMA_SYMBOL)) {
            val nextPoint = point() ?: return null
            points.add(nextPoint)
        }

        if (!expect(RPAREN_SYMBOL)) return null
        return Poliline(points)
    }

    private fun polispline(): Polispline? {
        if (!accept(POLISPLINE_SYMBOL)) return null
        if (!expect(LPAREN_SYMBOL)) return null
        val bends = mutableListOf<Bend>()

        val first = bend() ?: return null
        bends.add(first)

        while (accept(COMMA_SYMBOL)) {
            val next = bend() ?: return null
            bends.add(next)
        }

        if (!expect(RPAREN_SYMBOL)) return null
        return Polispline(bends)
    }

    private fun bend(): Bend? {
        if (!accept(BEND_SYMBOL)) return null
        if (!expect(LPAREN_SYMBOL)) return null
        val start = point() ?: return null
        if (!expect(COMMA_SYMBOL)) return null
        val end = point() ?: return null
        if (!expect(COMMA_SYMBOL)) return null
        val angle = expr() ?: return null
        if (!expect(RPAREN_SYMBOL)) return null
        return Bend(start, end, angle)
    }

    private fun line(): Line? {
        if (!accept(LINE_SYMBOL)) return null
        if (!expect(LPAREN_SYMBOL)) return null
        val start = point() ?: return null
        if (!expect(COMMA_SYMBOL)) return null
        val end = point() ?: return null
        if (!expect(RPAREN_SYMBOL)) return null
        return Line(start, end)
    }

    private fun simpleAttraction(symbol: Int): Pair<String, Point>? {
        if (!accept(symbol)) return null
        val name = token.lexeme.trim('"')
        if (!expect(STRING_LITERAL_SYMBOL)) return null
        val point = point() ?: return null
        return Pair(name, point)
    }
}

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token.symbol != EOF_SYMBOL) {
        print("${name(token.symbol)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}

fun main() {
    val file = File("input.txt").readText()
    printTokens(Scanner(ForForeachFFFAutomaton, file.byteInputStream()))
    val scanner = Scanner(ForForeachFFFAutomaton, file.byteInputStream())
    val parser = Parser(scanner)
    println("\n" + parser.parse())
}