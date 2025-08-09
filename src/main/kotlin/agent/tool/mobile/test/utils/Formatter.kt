package agent.tool.mobile.test.utils

object Formatter {
    /**
     * Converts a given string into a URL-friendly slug.
     *
     * Example:
     *  "Create a PosT" -> "create-a-post"
     *
     * Steps:
     *  - Lowercases the string
     *  - Removes non-alphanumeric characters (except spaces)
     *  - Trims leading/trailing spaces
     *  - Replaces one or more spaces with a single hyphen
     */
    fun String.formatToSlug() = lowercase()
        .replace(Regex("[^a-z0-9\\s]"), "") // remove special characters
        .trim()
        .replace(Regex("\\s+"), "-") // replace spaces with hyphen
}