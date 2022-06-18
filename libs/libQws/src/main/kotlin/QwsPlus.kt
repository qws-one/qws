// libs/libQws/src/main/kotlin/SimpleScript.kt


class QWS {
    class SimpleScript {

        class Result {
            var str = ""

            @Suppress("NOTHING_TO_INLINE")
            inline operator fun invoke(str: String) = put(str)

            infix fun put(str: String) {
                this.str = str
            }
        }

        class Parameters(val scriptFile: java.io.File)

        class Runtime(val parameters: Parameters, val result: Result)

        companion object {
            fun simpleScript(args: Array<String>, block: Runtime.() -> Unit): String {
                val scriptPath = args[0]
                val result = Result()
                Runtime(
                    Parameters(
                        scriptFile = java.io.File(scriptPath),
                    ),
                    result
                ).block()

                return result.str
            }
        }
    }
}