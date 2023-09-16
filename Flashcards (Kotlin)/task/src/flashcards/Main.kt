package flashcards

import java.io.File
import kotlin.random.Random

const val CARD_DATA_TYPE_TERM = "card"
const val CARD_DATA_TYPE_DEFINITION = "definition"

const val ACTION_ADD = "add"
const val ACTION_REMOVE = "remove"
const val ACTION_IMPORT = "import"
const val ACTION_EXPORT = "export"
const val ACTION_ASK = "ask"
const val ACTION_EXIT = "exit"
const val ACTION_LOG = "log"
const val ACTION_HARDEST_CARD = "hardest card"
const val ACTION_RESET_STATS = "reset stats"

var log = ""
val cards = mutableMapOf<String, String>()
val stats = mutableMapOf<String, Int>()
var importOnStart: String? = null
var exportOnExit: String? = null
fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "-import") {
        importOnStart = args[1]
    }
    if (args.isNotEmpty() && args[0] == "-export") {
        exportOnExit = args[1]
    }

    if (args.isNotEmpty() && args.size >= 4 && args[2] == "-import") {
        importOnStart = args[3]
    }
    if (args.isNotEmpty() && args.size >= 4 && args[2] == "-export") {
        exportOnExit = args[3]
    }


    importFromFile(importOnStart)

//    cards["france"] = "paris"
//    cards["poland"] = "warsaw"
    menu(cards)
}

fun printLine(message: String?) {
    if (message === null) {
        println()
        log += "\n"
    } else {
        println(message)
        log += "$message\n"
    }
}

fun readLine(): String {
    val read = readln()
    log += "$read\n"
    return read
}

fun menu(cards: MutableMap<String, String>) {
    printLine("Input the action ($ACTION_ADD, $ACTION_REMOVE, $ACTION_IMPORT, $ACTION_EXPORT, $ACTION_ASK, $ACTION_EXIT, $ACTION_LOG, $ACTION_HARDEST_CARD, $ACTION_RESET_STATS):")
    val action = readLine()
    when (action) {
        ACTION_ADD -> add()
        ACTION_REMOVE -> remove()
        ACTION_IMPORT -> import()
        ACTION_EXPORT -> export()
        ACTION_ASK -> ask()
        "display" -> display()
        ACTION_LOG -> log()
        ACTION_HARDEST_CARD -> hardestCard()
        ACTION_RESET_STATS -> resetStats()
        else -> {
            printLine("Bye Bye!")
            exportToFile(exportOnExit)
            return
        }
    }
    menu(cards)
}


fun log() {
    printLine("File name:")
    val fileName = readLine()

    val file = File(fileName)
    file.writeText(log)

    printLine("The log has been saved.")
}

fun hardestCard() {
    if (stats.isEmpty()) {
        printLine("There are no cards with errors.")
        return
    }

    val max = stats.maxByOrNull { it.value }
    val hardestTerms = mutableListOf<String>()

    for (term in stats) {
        if (term.value == max?.value) {
            hardestTerms.add(term.key)
        }
    }

    val label = if (hardestTerms.size > 1) "cards are" else "card is"
    val labelSecond = if (hardestTerms.size > 1) "them" else "it"
    printLine(
        "The hardest $label ${
            hardestTerms.joinToString(
                "\", \"", "\"", "\""
            )
        }. You have ${max?.value} errors answering $labelSecond."
    )
}

fun resetStats() {
    stats.clear()
    printLine("Card statistics have been reset.")
}

fun readCardData(dataType: String): String? {
    val data = readLine()

    val valid = when (dataType) {
        CARD_DATA_TYPE_TERM -> !cards.containsKey(data)
        else -> !cards.containsValue(data)
    }

    if (valid) {
        return data
    }

    printLine("The $dataType \"$data\" already exists.")
    return null
}

fun add() {
    printLine("The card:")
    val term = readCardData(CARD_DATA_TYPE_TERM)

    if (term === null) {
        return
    }

    printLine("The definition of the card:")
    val definition = readCardData(CARD_DATA_TYPE_DEFINITION)

    if (definition === null) {
        return
    }

    cards[term] = definition
    printLine("The pair (\"$term\":\"$definition\") has been added")
}

fun remove() {
    printLine("Which card?")
    val card = readLine()
    if (cards.containsKey(card)) {
        printLine("The card has been removed.")
        cards.remove(card)
    } else {
        printLine("Can't remove \"$card\": there is no such card.")
    }
}

fun import() {
    printLine("File name:")
    val fileName = readLine()
    importFromFile(fileName)
}

fun importFromFile(importOnStart: String?) {
    if (importOnStart === null) {
        return
    }
    val file = File(importOnStart)
    if (!file.exists()) {
        printLine("File not found.")
        return
    }

    var count = 0
    file.forEachLine {
        val (term, definition, wrongAnswers) = it.split(":")
        cards[term] = definition
        if (wrongAnswers.toInt() > 0) {
            stats[term] = wrongAnswers.toInt()
        }
        count++
    }
    printLine("$count cards have been loaded.")
}

fun export() {
    printLine("File name:")
    val fileName = readLine()
    exportToFile(fileName)
}


fun exportToFile(exportOnExit: String?) {
    if (exportOnExit === null) {
        return
    }
    val file = File(exportOnExit)
    file.writeText("")
    for (card in cards) {
        file.appendText("${card.key}:${card.value}:${stats[card.key] ?: 0}\n")
    }

    printLine("${cards.size} cards have been saved.")
}

fun display() {
    printLine(cards.toString())
    println(stats)
}

fun ask() {
    val keys = cards.keys.toList()

    printLine("How many times to ask?")
    val i = readLine().toInt()
    repeat(i) {
        val rand = Random.nextInt(keys.size)
        val term = keys[rand]
        val definition = cards[term]

        printLine("Print the definition of \"$term\":")
        val answer = readLine()

        if (definition == answer) {
            printLine("Correct!")
        } else {
            print("Wrong. The right answer is \"$definition\"")
            stats[term] = (stats[term] ?: 0) + 1
            cards.forEach { (key2, value2) ->
                if (value2 == answer) {
                    print(", but your definition is correct for \"$key2\"")
                }
            }

            printLine(".")
        }
    }
}