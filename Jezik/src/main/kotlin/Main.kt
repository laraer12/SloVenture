package org.example

import java.io.File
import java.io.InputStream
import kotlin.math.exp

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
const val SECONDC_SYMBOL =17
const val STRING_LITERAL_SYMBOL =18
const val NUM_SYMBOL = 19
const val LBRACE_SYMBOL = 20
const val RBRACE_SYMBOL = 21
const val LPAREN_SYMBOL =22
const val RPAREN_SYMBOL = 23
const val COMMA_SYMBOL =24
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
    fun symbol(state:Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object ForForeachFFFAutomaton: DFA {
    override val states = (1 ..19).toSet()
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19)

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) {IntArray(numberOfCodes)}
    private val values = Array(numberOfStates) {SKIP_SYMBOL}

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
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
        for(c in 'A'..'Z') setTransition(1,c,2)
        for(c in 'A'..'Z') setTransition(2, c, 2)
        setSymbol(2, PVAR_SYMBOL)

        //VAR IN RESERVED WORDS
        for (c in 'a'..'z') setTransition(1, c, 3)
        for (c in 'a'..'z') setTransition(2, c, 3)
        for (c in '0'..'9') setTransition(2, c, 3)
        for(c in 'A'..'Z') setTransition(3,c,3)
        for(c in 'a'..'z') setTransition(3,c,3)
        for(c in 'A'..'Z') setTransition(3,c,3)


        //NAMES - STRING LITERAL
        setTransition(1, '"', 4)
        for(c in 'A'..'Z') setTransition(4,c,4)
        for(c in 'a'..'z') setTransition(4,c,4)
        setTransition(4,' ',4)
        setTransition(4,'"',5)
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
        last = code // The code following the current lexeme is the first code of the next lexeme

        if (automaton.finalStates.contains(state)) {
            val lexeme = String(buffer.toCharArray())
            val symbol = if(state == 3) reservedWord(lexeme) else automaton.symbol(state)
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

fun reservedWord(lexeme: String): Int{
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

class Parser(private val scanner: Scanner){
    private var token: Token  = scanner.getToken()

    private fun accept(symbol: Int): Boolean {
        if(token.symbol == symbol){
            token = scanner.getToken()
            return true
        }
        return false
    }

    private fun expect(symbol: Int): Boolean {
        if(!accept(symbol)){
            println("Syntax error at ${token.startRow}:${token.startColumn}, expected ${name(symbol)}, got ${name(token.symbol)}")
            return false
        }
        return true
    }

    fun parse(): Boolean {
        val success = program()
        if(token.symbol != EOF_SYMBOL){
            println("Unexpected token after: ${token.lexeme}")
            return false
        }
        return true
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
        return region() || declaration() || path()
    }

    private fun region(): Boolean {
        if(!accept(REGION_SYMBOL)) return false
        if(!expect(STRING_LITERAL_SYMBOL)) return false
        if (!expect(LBRACE_SYMBOL)) return false
        if (!regionArea()) return false
        if (!attractions()) return false
        return expect(RBRACE_SYMBOL)
    }

    private fun regionArea(): Boolean{
        return poliline()
    }

    private fun attractions(): Boolean{
        while (true) {
            if (token.symbol in listOf(RBRACE_SYMBOL, EOF_SYMBOL)) break
            if (!attraction()) return false
        }
        return true
    }

    private fun attraction(): Boolean {
        return hill() || cabin() || church() || mountain() || other() || castle() || lake() || nearby() || declaration()
    }

    private fun hill(): Boolean {
        if (!simpleAttraction(HILL_SYMBOL)) return false
        return poliline()
    }

    private fun cabin(): Boolean {
        if(!simpleAttraction(CABIN_SYMBOL)) return false
        return box()
    }

    private fun church(): Boolean {
        if(!simpleAttraction(CHURCH_SYMBOL)) return false
        return box()
    }


    private fun mountain(): Boolean{
        if(! simpleAttraction(MOUNTAIN_SYMBOL)) return false
        return poliline()
    }


    private fun other(): Boolean {
        return simpleAttraction(OTHER_SYMBOL)
    }

    private fun castle(): Boolean{
        if(!simpleAttraction(CASTLE_SYMBOL)) return false
        return box()
    }

    private fun lake(): Boolean{
        if(!simpleAttraction(LAKE_SYMBOL)) return false
        return circle()
    }

    private fun nearby(): Boolean{
        if(!accept(NEARBY_SYMBOL)) return false
        if(!expect(LPAREN_SYMBOL)) return false
        if(!point()) return false
        if(!expect(COMMA_SYMBOL)) return false
        if(!expr()) return false
        return expect(RPAREN_SYMBOL)
    }

    private fun declaration(): Boolean{
        return pvarDeclaration() || varDeclaration()
    }

    private fun pvarDeclaration(): Boolean{
        if(!accept(PVAR_DEC_SYMBOL)) return false
        if(!expect(PVAR_SYMBOL)) return false
        if(!expect(ASSIGN_SYMBOL)) return false
        return point()
    }

    private fun varDeclaration(): Boolean {
        if(!accept(NVAR_DEC_SYMBOL)) return false
        if(!expect(VAR_SYMBOL)) return false
        if(!expect(ASSIGN_SYMBOL)) return false
        return expr()
    }

    private fun expr(): Boolean{
        if(!term()) return false
        while(token.symbol in listOf(PLUS_SYMBOL, MINUS_SYMBOL, TIMES_SYMBOL, DIVIDE_SYMBOL)){
            token = scanner.getToken()
            if(!term())return false
        }
        return true
    }

    private fun term(): Boolean{
        return when (token.symbol) {
            FIRSTC_SYMBOL -> {
                token = scanner.getToken()
                expect(LPAREN_SYMBOL) && expect(PVAR_SYMBOL) && expect(RPAREN_SYMBOL)
            }
            SECONDC_SYMBOL -> {
                token = scanner.getToken()
                expect(LPAREN_SYMBOL) && expect(PVAR_SYMBOL) && expect(RPAREN_SYMBOL)
            }
            NUM_SYMBOL, VAR_SYMBOL -> {
                token = scanner.getToken(); true
            }
            else -> false
        }
    }

    private fun point(): Boolean{
        return if (accept(POINT_SYMBOL)) {
            expect(LPAREN_SYMBOL) && expr() && expect(COMMA_SYMBOL) && expr() && expect(RPAREN_SYMBOL)
        } else if (accept(PVAR_SYMBOL)) {
            true
        } else {
            false
        }
    }

    private fun box(): Boolean{
        return expect(BOX_SYMBOL) && expect(LPAREN_SYMBOL) && point() && expect(COMMA_SYMBOL) && point() && expect(
            RPAREN_SYMBOL)
    }

    private fun circle(): Boolean{
        return expect(CIRCLE_SYMBOL) && expect(LPAREN_SYMBOL) && point() && expect(COMMA_SYMBOL) && expr() && expect(
            RPAREN_SYMBOL)
    }

    private fun path(): Boolean{
        if(!accept(PATH_SYMBOL)) return false
        if(!expect(LBRACE_SYMBOL))return false
        if (!instructions()) return false
        return expect(RBRACE_SYMBOL)
    }

    private fun instructions(): Boolean{
        while (true){
            if(!instruction()) return false
            if (token.symbol == RBRACE_SYMBOL) break
        }
        return true
    }

    private fun instruction(): Boolean{
        return poliline() || polispline() || line() || bend()
    }

    private fun poliline(): Boolean{
        if(!accept(POLILINE_SYMBOL))return false
        if (!expect(LPAREN_SYMBOL)) return false
        if(!point())return false
        while (accept(COMMA_SYMBOL)){
            if(!point())return false
        }
        return expect(RPAREN_SYMBOL)
    }

    private fun polispline(): Boolean {
        if (!accept(POLISPLINE_SYMBOL)) return false
        if (!expect(LPAREN_SYMBOL)) return false
        if (!bend()) return false
        while (accept(COMMA_SYMBOL)){
            if(!bend())return false
        }
        return expect(RPAREN_SYMBOL)
    }

    private fun bend(): Boolean{
        if (!accept(BEND_SYMBOL)) return false
        if(!expect(LPAREN_SYMBOL)) return false
        if(!point()) return false
        if(!expect(COMMA_SYMBOL)) return false
        if (!point())return false
        if (!expect(COMMA_SYMBOL))return false
        if (!expr())return false
        return expect(RPAREN_SYMBOL)
    }

    private fun line(): Boolean {
        if(!accept(LINE_SYMBOL)) return false
        if(!expect(LPAREN_SYMBOL)) return false
        if(!point()) return false
        if(!expect(COMMA_SYMBOL))return false
        if (!point())return false
        return expect(RPAREN_SYMBOL)
    }

    private fun simpleAttraction(symbol: Int): Boolean{
        if(!accept(symbol)) return false
        if(!expect(STRING_LITERAL_SYMBOL)) return false
        if(!point()) return false
        return true
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
    if(parser.parse()){
        println("\naccept")
    }else{
        println("reject")
    }
}