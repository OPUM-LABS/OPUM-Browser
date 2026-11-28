package com.mycompany.app.db.book

/**
 * One saved address (2026-11-28). [street] holds the street with the house number ("Bahnhofstrasse 5"); a form
 * that asks for the number on its own gets it split off ([splitStreet]). [country] is what the form showed (the
 * name in a list, or what was typed); [countryCode] the two-letter code when the form's list used codes.
 */
data class SavedAddress(
    val id: Long = 0,
    val label: String = "",
    val name: String = "",
    val org: String = "",
    val street: String = "",
    val line2: String = "",
    val postcode: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "",
    val countryCode: String = "",
    val tel: String = "",
    val email: String = "",
) {
    /** The fields that can be filled into a page, by the key the page scripts use. */
    fun fields(): Map<String, String> = mapOf(
        "name" to name, "org" to org, "street" to street, "line2" to line2, "postcode" to postcode, "city" to city,
        "region" to region, "country" to country, "cc" to countryCode, "tel" to tel, "email" to email,
    )

    val isEmpty: Boolean get() = fields().values.all { it.isBlank() }

    /** How many of the fields that make it an address (street, postcode, city, region, country) are set. */
    val addressParts: Int get() = listOf(street, postcode, city, region, country.ifBlank { countryCode }).count { it.isNotBlank() }

    /** Title in lists and bars: the label, else the name, else the street. */
    fun title(): String = label.trim().ifEmpty { name.trim() }.ifEmpty { street.trim() }

    /** One line: name, street, postcode and city (what tells two addresses apart). */
    fun summary(): String =
        listOf(name, street, listOf(postcode, city).filter { it.isNotBlank() }.joinToString(" "))
            .map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")

    /** Every field that is set here is set the same in [saved]: nothing new to save. */
    fun coveredBy(saved: SavedAddress): Boolean {
        val mine = fields()
        val theirs = saved.fields()
        return mine.all { (key, value) -> value.isBlank() || norm(value) == norm(theirs[key] ?: "") }
    }

    /** The same place as [other] (street and postcode, or street and name, alike): a change of it, not a new one. */
    fun samePlace(other: SavedAddress): Boolean {
        if (street.isBlank() || norm(street) != norm(other.street)) return false
        return (postcode.isNotBlank() && norm(postcode) == norm(other.postcode)) ||
            (name.isNotBlank() && norm(name) == norm(other.name))
    }

    /** [saved] with the fields set here written over it (its id and label stay). */
    fun mergedInto(saved: SavedAddress): SavedAddress = saved.copy(
        name = name.ifBlank { saved.name }, org = org.ifBlank { saved.org }, street = street.ifBlank { saved.street },
        line2 = line2.ifBlank { saved.line2 }, postcode = postcode.ifBlank { saved.postcode },
        city = city.ifBlank { saved.city }, region = region.ifBlank { saved.region },
        country = country.ifBlank { saved.country }, countryCode = countryCode.ifBlank { saved.countryCode },
        tel = tel.ifBlank { saved.tel }, email = email.ifBlank { saved.email },
    )

    companion object {
        /** Longest value kept per field (a page reports what was typed; nothing an address needs is longer). */
        const val MAX_FIELD = 200

        private fun norm(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), " ")

        /**
         * [street] as street and house number for forms that ask for them apart: the number at the end
         * ("Bahnhofstrasse 5b", "Via Roma 12/3") or at the start ("221B Baker Street", "12 rue de la Paix"). Without a
         * number, the street and "".
         */
        fun splitStreet(street: String): Pair<String, String> {
            val s = street.trim()
            Regex("^(.*\\D)[\\s,]+(\\d+\\s?[a-zA-Z]?(?:\\s?[-/]\\s?\\d+[a-zA-Z]?)?)$").find(s)?.let {
                return it.groupValues[1].trim().trimEnd(',') to it.groupValues[2].trim()
            }
            Regex("^(\\d+\\s?[a-zA-Z]?(?:[-/]\\d+[a-zA-Z]?)?)[\\s,]+(\\D.*)$").find(s)?.let {
                return it.groupValues[2].trim() to it.groupValues[1].trim()
            }
            return s to ""
        }

        /** [name] as given and family name: the last word is the family name. */
        fun splitName(name: String): Pair<String, String> {
            val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.size < 2) return name.trim() to ""
            return words.dropLast(1).joinToString(" ") to words.last()
        }
    }
}
