package com.mycompany.app.data.transfer

/** One saved login as the browsers' CSV files carry it. */
data class PasswordEntry(
    val url: String,
    val username: String,
    val password: String,
    val name: String = "",
    val note: String = "",
)

/** Result of reading a password CSV: the usable [entries] and the number of data rows that could not be used. */
data class PasswordCsvResult(val entries: List<PasswordEntry>, val failed: Int, val headerFound: Boolean)

/**
 * Reads the password CSV files of the common browsers and writes Chrome's format. Plain Kotlin, unit tested.
 *
 * - Chrome / Edge / Brave / Opera: `name,url,username,password,note` (older versions without `note`).
 * - Firefox: `"url","username","password","httpRealm","formActionOrigin","guid","timeCreated","timeLastUsed",
 *   "timePasswordChanged"`.
 * - Columns are found by their header name (case-insensitive), so other orders and a few other tools' names
 *   (`origin`, `login_uri`, `login_username`, …) work too.
 *
 * The reader follows RFC 4180: fields in double quotes may hold commas, line breaks and `""` for a quote; a UTF-8
 * byte order mark is dropped; CRLF and LF line ends both work.
 */
object PasswordCsv {

    const val CHROME_HEADER = "name,url,username,password,note"

    private val URL_NAMES = listOf("url", "origin", "origin_url", "login_uri", "website", "web site", "uri")
    private val USER_NAMES = listOf("username", "login_username", "user", "login", "username_value", "email")
    private val PASS_NAMES = listOf("password", "login_password", "password_value", "pass")
    private val NAME_NAMES = listOf("name", "title")
    private val NOTE_NAMES = listOf("note", "notes", "comment", "comments")

    fun parse(text: String): PasswordCsvResult {
        val rows = parseRows(text)
        if (rows.isEmpty()) return PasswordCsvResult(emptyList(), 0, false)
        val header = rows[0].map { it.trim().lowercase() }
        fun col(names: List<String>) = names.asSequence().map { header.indexOf(it) }.firstOrNull { it >= 0 } ?: -1
        val urlCol = col(URL_NAMES)
        val userCol = col(USER_NAMES)
        val passCol = col(PASS_NAMES)
        if (urlCol < 0 || passCol < 0) return PasswordCsvResult(emptyList(), rows.size - 1, false)
        val nameCol = col(NAME_NAMES)
        val noteCol = col(NOTE_NAMES)
        val entries = ArrayList<PasswordEntry>()
        var failed = 0
        for (r in 1 until rows.size) {
            val row = rows[r]
            if (row.all { it.isBlank() }) continue
            val url = row.getOrNull(urlCol)?.trim().orEmpty()
            val password = row.getOrNull(passCol).orEmpty()
            if (url.isEmpty() || password.isEmpty()) {
                failed++
                continue
            }
            entries.add(
                PasswordEntry(
                    url = url,
                    username = if (userCol >= 0) row.getOrNull(userCol).orEmpty() else "",
                    password = password,
                    name = if (nameCol >= 0) row.getOrNull(nameCol).orEmpty() else "",
                    note = if (noteCol >= 0) row.getOrNull(noteCol).orEmpty() else "",
                ),
            )
        }
        return PasswordCsvResult(entries, failed, true)
    }

    /** Chrome's CSV (`name,url,username,password,note`), fields quoted where needed, LF line ends. */
    fun write(entries: List<PasswordEntry>): String {
        val sb = StringBuilder()
        sb.append(CHROME_HEADER).append('\n')
        for (e in entries) {
            sb.append(quote(e.name)).append(',')
                .append(quote(e.url)).append(',')
                .append(quote(e.username)).append(',')
                .append(quote(e.password)).append(',')
                .append(quote(e.note)).append('\n')
        }
        return sb.toString()
    }

    fun quote(field: String): String {
        val needs = field.any { it == ',' || it == '"' || it == '\n' || it == '\r' } ||
            (field.isNotEmpty() && (field.first().isWhitespace() || field.last().isWhitespace()))
        return if (!needs) field else "\"" + field.replace("\"", "\"\"") + "\""
    }

    /** All records of [text]; each record is its list of fields. */
    fun parseRows(text: String): List<List<String>> {
        val src = if (text.startsWith("﻿")) text.substring(1) else text
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val field = StringBuilder()
        var inQuotes = false
        var fieldStarted = false
        var i = 0
        val n = src.length
        fun endField() {
            row.add(field.toString())
            field.setLength(0)
            fieldStarted = false
        }
        fun endRow() {
            endField()
            rows.add(row)
            row = ArrayList()
        }
        while (i < n) {
            val c = src[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < n && src[i + 1] == '"') {
                        field.append('"')
                        i += 2
                        continue
                    }
                    inQuotes = false
                } else {
                    field.append(c)
                }
                i++
                continue
            }
            when (c) {
                '"' -> {
                    if (!fieldStarted && field.isEmpty()) inQuotes = true else field.append(c)
                    fieldStarted = true
                }
                ',' -> endField()
                '\r' -> {
                    endRow()
                    if (i + 1 < n && src[i + 1] == '\n') i++
                }
                '\n' -> endRow()
                else -> {
                    field.append(c)
                    fieldStarted = true
                }
            }
            i++
        }
        if (fieldStarted || field.isNotEmpty() || row.isNotEmpty()) endRow()
        return rows
    }
}
