//

@Suppress("MemberVisibilityCanBePrivate")
object Util {

    fun sha1sum(str: String): String? = java.security.MessageDigest.getInstance("SHA-1")?.run {
        reset()
        update(str.toByteArray())
        val sum = java.lang.String.format("%040x", java.math.BigInteger(1, digest()))
        reset()
        sum
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