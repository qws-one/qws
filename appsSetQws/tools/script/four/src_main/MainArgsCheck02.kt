object MainArgsCheck02 {
    interface ScriptStr {

        fun script(runtimeMap: Map<String, Any?>): String

        @Suppress("unused", "UNCHECKED_CAST")
        companion object {
            @Suppress("NOTHING_TO_INLINE")
            class Result {
                var str = ""
                inline operator fun invoke(str: String) = put(str)
                inline infix fun put(stringResult: String) {
                    str = stringResult
                }
            }

            class ValueFrom<T>(inline val valueByName: (String) -> T) {
                operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T = valueByName(property.name)
            }

            class ValueFromThisRefOfMapWithDefault<T>(inline val getDefault: (String) -> T) {
                operator fun getValue(map: Map<*, *>, property: kotlin.reflect.KProperty<*>): T {
                    return if (map.containsKey(property.name)) map[property.name] as T else getDefault(property.name)
                }
            }

            class ValueFromThisRef0<T> {
                operator fun getValue(thisRef: Map<*, *>, property: kotlin.reflect.KProperty<*>): T =
                    thisRef.also { println("get0 " + property.name) }[property.name] as T
            }

            object ValueFromThisRef {
                operator fun <T> getValue(thisRef: Map<*, *>, property: kotlin.reflect.KProperty<*>): T =
                    thisRef.also { println("get  " + property.name) }[property.name] as T
            }

            private val stringValue = ValueFromThisRefOfMapWithDefault { "" }

            class Runtime(val scriptStrResult: Result) {
                val Map<String, Any?>.bindings: Map<String, Any?> by ValueFromThisRefOfMapWithDefault { emptyMap() }
                val Map<String, Any?>.scriptPath: String by stringValue
                val Map<String, Any?>.tmpDirBig: String by stringValue
                val Map<String, Any?>.tmpDirQuick: String by stringValue
                val Map<String, Any?>.args: List<String> by ValueFromThisRefOfMapWithDefault { emptyList() }
            }

            operator fun invoke(block: Runtime.() -> Unit): String {
                val result = Result()
                Runtime(result).block()
                return result.str
            }
        }
    }

    private object MainArgsCheck : ScriptStr {
        private val className = this::class.simpleName

        override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
            println("... $className")
            println("MainArgsCheck.script bindings   =})>${runtimeMap.bindings}<({")
            println("MainArgsCheck.script scriptPath =})>${runtimeMap.scriptPath}<({")
            println("MainArgsCheck.script args       =})>${runtimeMap.args}<({")
            println("MainArgsCheck.script tmpDirBig  =})>${runtimeMap.tmpDirBig}<({")
            println("MainArgsCheck.script tmpDirQuick=})>${runtimeMap.tmpDirQuick}<({")

            runtimeMap.args
            runtimeMap.args

            scriptStrResult put "$className ..."
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val res = MainArgsCheck.script(
            mapOf(
                "bindings" to mapOf("one" to "two"),
                "scriptPath" to " one/two ",
                "tmpDirQuick" to "/one/two ",
                "args" to listOf("one", "two"),
            )
        )
        println("MainArgsCheck02.main res=$res")
    }
}