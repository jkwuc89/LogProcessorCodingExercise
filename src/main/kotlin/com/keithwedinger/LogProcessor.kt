package com.keithwedinger

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess
import java.util.stream.Collectors



/**
 * LogProcessor Coding Exercise
 *
 * @author Keith Wedinger <br>
 * Created On: 2/1/18
 */

fun main(args: Array<String>) {
    if (args.size == 1) {
        val logEntriesFile = File(args[0])
        if (logEntriesFile.isFile) {
            // Read the log entries file into a list of log entries
            val logEntryReader = logEntriesFile.bufferedReader()
            val logEntries = mutableListOf<String>()
            logEntryReader.useLines { lines -> lines.forEach { logEntries.add(it) } }

            // Parse the log entries
            val logProcessor = LogProcessor()
            logProcessor.parseLogEntries(logEntries)

            // Report on the parsing results...

            // Most frequently requested URL overall
            var mostFrequentlyRequestedUrl: Pair<String, Long>?
            mostFrequentlyRequestedUrl = logProcessor.getMostFrequentlyRequestedUrl()
            println("Most frequently requested URL:\nURL: ${mostFrequentlyRequestedUrl.first}\nCount: ${mostFrequentlyRequestedUrl.second}")

            print("\n")

            // Most frequently requested URL by specific date
            val lookupDate = "1995-07-01"
            mostFrequentlyRequestedUrl = logProcessor.getMostFrequentlyRequestedUrlForDate(lookupDate)
            if (mostFrequentlyRequestedUrl != null) {
                println("Most frequently requested URL on ${lookupDate}:\nURL: ${mostFrequentlyRequestedUrl.first}\nCount: ${mostFrequentlyRequestedUrl.second}")
            } else {
                println("No log entries exist for date: ${lookupDate}")
            }

            print("\n")

            // Most frequently requested URL by date
            val mostFrequentlyRequestedUrlsByDate = logProcessor.getMostFrequentlyRequestedUrlForEachDate()
            println("Most frequently requested URLs by each date")
            println("Date        URL (request count)")
            println("----------  -------------------")
            mostFrequentlyRequestedUrlsByDate.forEach {
                println("${logProcessor.logDateFormat.format(it.first)}  ${it.second.key} (${it.second.value})")
            }

        } else {
            println("ERROR: ${args[0]} does not exist")
            exitProcess(1)
        }
    } else {
        println("Usage: java -jar LogProcessor.jar <log entry filename>")
        exitProcess(1)
    }
}

class LogProcessor {
    // Regex to get date and URL: group 1 = date, group 2 = URL
    private val dateAndUrlRegex = "\\[(.*)T.*\"[A-Z]+\\s([^\\s]+)\\s".toRegex()
    var logDateFormat = SimpleDateFormat("yyyy-MM-dd")

    private val urlToCountMap = HashMap<String, Long>()
    private val dateToOccurrencesMap = HashMap<Date, HashMap<String, Long>>()

    /**
     * Parse the log entries into HashMaps defined above
     */
    fun parseLogEntries(logEntries: List<String>) {
        logEntries.forEach({ logEntry ->
            val (logDate, logUrl) = getDateAndUrlFromLogEntry(logEntry)

            // Break down log entry occurrences by date
            val urlToCountMapByDate: HashMap<String, Long>
            if (dateToOccurrencesMap.containsKey(logDate)) {
                urlToCountMapByDate = dateToOccurrencesMap[logDate]!!
                if (urlToCountMapByDate.containsKey(logUrl)) {
                    val count = urlToCountMapByDate[logUrl]
                    urlToCountMapByDate[logUrl] = count!! + 1
                } else {
                    urlToCountMapByDate[logUrl] = 1
                }
            } else {
                urlToCountMapByDate = HashMap()
                urlToCountMapByDate[logUrl] = 1
                dateToOccurrencesMap[logDate] = urlToCountMapByDate
            }

            // Break down log entries to total occurrences
            if (urlToCountMap.containsKey(logUrl)) {
                val count = urlToCountMap[logUrl]!! + 1
                urlToCountMap[logUrl] = count
            } else {
                urlToCountMap[logUrl] = 1
            }
        })
    }

    /**
     * Get the most frequently requested URL
     * @returns Pair containing URL and its request count
     */
    fun getMostFrequentlyRequestedUrl(): Pair<String, Long> {
        // Kotlin helper that finds map entry with maximum value
        val maxCountEntry = urlToCountMap.maxBy { it.value }
        return Pair(maxCountEntry!!.key, maxCountEntry.value)
    }

    /**
     * Get the most frequently requested URL for a specific date
     * @input logDate String using format: YYYY-MM-DD
     * @return Pair containing the URL and its request count or null
     *         if no entries exist for that date
     */
    fun getMostFrequentlyRequestedUrlForDate(logDate: String): Pair<String, Long>? {
        val entriesDate = logDateFormat.parse(logDate)
        var maxCountEntry: Map.Entry<String, Long>? = null
        return if (dateToOccurrencesMap.containsKey(entriesDate)) {
            val occurrencesMap = dateToOccurrencesMap[entriesDate]!!
            maxCountEntry = occurrencesMap.maxBy { it.value }!!
            Pair(maxCountEntry.key, maxCountEntry.value)
        } else {
            null
        }
    }

    /**
     * Return list of most requently access URLs by date, sorted by date descending
     */
    fun getMostFrequentlyRequestedUrlForEachDate(): List<Pair<Date, Map.Entry<String, Long>>> {
        val mostFrequentUrlsByDate = dateToOccurrencesMap
            .map { occurrencesMap ->
                val occurrencesDate = occurrencesMap.key
                val maxCountEntry = occurrencesMap.value.maxBy { it.value }!!
                Pair(occurrencesDate, maxCountEntry)
            }
        return mostFrequentUrlsByDate.sortedWith(compareBy({it.first}))
    }

    /**
     * Get the date and URL from a log entry
     * @throws IllegalArgumentException if log entry is malformed
     * @return Pair<Date, String>
     */
    private fun getDateAndUrlFromLogEntry(logEntry: String): Pair<Date, String> {
        val matchResult = dateAndUrlRegex.find(logEntry)
        val logDate: Date
        val logUrl: String

        // Get the log entry date and URL via regex above
        if (matchResult != null) {
            logDate = logDateFormat.parse(matchResult.groups[1]!!.value)
            logUrl = matchResult.groups[2]!!.value
        } else {
            throw IllegalArgumentException("${logEntry} is malformed")
        }
        return Pair(logDate, logUrl)
    }
}