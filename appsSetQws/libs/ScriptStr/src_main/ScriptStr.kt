//

interface ScriptStr {

    fun script(runtimeMap: Map<String, Any?>): String

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        class Result {
            var str = ""
            inline operator fun invoke(str: String) = put(str)
            inline infix fun put(stringResult: String) {
                str = stringResult
            }
        }

        class Runtime(val scriptStrResult: Result)

        operator fun invoke(block: Runtime.() -> Unit): String {
            val result = Result()
            Runtime(result).block()
            return result.str
        }
    }
}