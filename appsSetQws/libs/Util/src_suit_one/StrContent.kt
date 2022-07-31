open class StrContent(val content: String) {
    val sha1sum = Util.sha1sum(content) ?: TODO()
}