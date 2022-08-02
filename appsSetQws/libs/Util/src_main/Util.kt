//

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Util {

    inline val String.capitalized: String get() = capitalize()
    fun String.capitalize() = if (isNotEmpty()) get(0).uppercaseChar() + substring(1) else this

    inline val String.sha1sum: String? get() = sha1sum(this)
    fun sha1sum(str: String): String? = java.security.MessageDigest.getInstance("SHA-1")?.run {
        reset()
        update(str.toByteArray())
        val sum = java.lang.String.format("%040x", java.math.BigInteger(1, digest()))
        reset()
        sum
    }

    fun <T> listOfValid(vararg items: T, isValid: T.() -> Boolean): List<T> {
        if (items.isEmpty()) return emptyList()
        val list = mutableListOf<T>()
        for (item in items) if (item.isValid()) list.add(item)
        return list
    }

    fun dependenciesSrc(file: java.io.File) = linesWithoutComments(file.readText())

    fun linesWithoutComments(text: String): List<String> {
        val result = mutableListOf<String>()
        for (line in text.lines()) line.trim().let {
            if (it.isNotEmpty() && !it.startsWith("//") && !it.startsWith("#"))
                result.add(it)
        }
        return result
    }
}