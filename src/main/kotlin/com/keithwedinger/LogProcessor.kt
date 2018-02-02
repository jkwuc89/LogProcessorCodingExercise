package com.keithwedinger

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

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

            // Report on the parsing results
            val (mostFrequentlyRequestedUrl, count) = logProcessor.getMostFrequentlyRequestedUrl()
            println("URL that was most frequently requested:\nURL: ${mostFrequentlyRequestedUrl}\nCount: ${count}")
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
    private val logDateFormat = SimpleDateFormat("yyyy-MM-dd")
    
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