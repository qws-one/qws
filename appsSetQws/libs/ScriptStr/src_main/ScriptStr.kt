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
            inline infix fun append(stringResult: String) {
                str += stringResult
            }
        }

        class ValueFromThisRefOfMap<T>(inline val getDefault: (String) -> T) {
            @Suppress("UNCHECKED_CAST")
            operator fun getValue(map: Map<*, *>, property: kotlin.reflect.KProperty<*>): T {
                return if (map.containsKey(property.name)) map[property.name] as T else getDefault(property.name)
            }
        }

        private val stringValue = ValueFromThisRefOfMap { "" }

        class Runtime(val scriptStrResult: Result) {
            val Map<String, Any?>.bindings: Map<String, Any?> by ValueFromThisRefOfMap { emptyMap() }
            val Map<String, Any?>.scriptName: String by stringValue
            val Map<String, Any?>.scriptPath: String by stringValue
            val Map<String, Any?>.appsPlacePath: String by stringValue
            val Map<String, Any?>.tmpDirBig: String by stringValue
            val Map<String, Any?>.tmpDirQuick: String by stringValue
            val Map<String, Any?>.args: List<String> by ValueFromThisRefOfMap { emptyList() }
        }

        operator fun invoke(block: Runtime.() -> Unit): String {
            val result = Result()
            Runtime(result).block()
            return result.str
        }
    }
}