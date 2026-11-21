package com.ugurbuga.previo.runtime

data class PreviewMetadata(
    val group: String,
    val order: Int,
    val alphabeticalOrder: String,
    val customInfo: String
)

object PreviewNameParser {
    // Format: [GROUP] [ORDER] [ALPHABETICAL_ORDER] [CUSTOM_INFO]
    private val regex = Regex("""^\[(.*?)\]\s*\[(.*?)\]\s*\[(.*?)\]\s*\[(.*?)\]$""")

    fun parse(name: String): PreviewMetadata {
        val trimmed = name.trim()
        val match = regex.matchEntire(trimmed)
            ?: throw IllegalArgumentException("Invalid preview name format: '$name'. Expected format: [GROUP] [ORDER] [ALPHABETICAL_ORDER] [CUSTOM_INFO]")

        val group = match.groupValues[1].trim()
        val orderStr = match.groupValues[2].trim()
        val order = orderStr.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid order in preview name '$name': '$orderStr' must be an integer.")
        val alphabeticalOrder = match.groupValues[3].trim()
        val customInfo = match.groupValues[4].trim()

        return PreviewMetadata(group, order, alphabeticalOrder, customInfo)
    }
}
