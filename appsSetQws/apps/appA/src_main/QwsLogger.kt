class QwsLogger(val investObj: Info.Instance) {
    interface BaseInterface {
        val logger: Interface
        fun msgWrapper(wrap: (String) -> String)

        companion object {
            val dummy = object : BaseInterface {
                override /*inline ? */ val logger: Interface
                    get() = Interface.dummy

                override fun msgWrapper(wrap: (String) -> String) {}
            }

            val systemOut = object : BaseInterface {
                var wrap: (String) -> String = { it }
                override val logger = object : Interface {
                    override infix fun info(any: Any?) {
                        println("${Severity.Info.name} ${_wrap(any)}")
                    }

                    override infix fun error(any: Any?) {
                        println("${Severity.Error.name} ${_wrap(any)}")
                    }

                    override fun warning(any: Any?) {
                        println("${Severity.Warning.name} ${_wrap(any)}")
                    }

                    override fun debug(any: Any?) {
                        println("${Severity.Debug.name} ${_wrap(any)}")
                    }
                }

                @Suppress("FunctionName", "NOTHING_TO_INLINE")
                private inline fun _wrap(any: Any?) = wrap(any?.toString() ?: "Nil")

                override fun msgWrapper(wrap: (String) -> String) {
                    this.wrap = wrap
                }
            }
        }
    }

    interface Interface {
        infix fun info(any: Any?)
        infix fun error(any: Any?)
        infix fun warning(any: Any?)
        infix fun debug(any: Any?)

        companion object {
            val dummy = object : Interface {
                override fun info(any: Any?) {}
                override fun error(any: Any?) {}
                override fun warning(any: Any?) {}
                override fun debug(any: Any?) {}
            }

            val systemOut = object : Interface {
                override fun info(any: Any?) {
                    println("${Severity.Info.name} $any")
                }

                override fun error(any: Any?) {
                    println("${Severity.Error.name} $any")
                }

                override fun warning(any: Any?) {
                    println("${Severity.Warning.name} $any")
                }

                override fun debug(any: Any?) {
                    println("${Severity.Debug.name} $any")
                }
            }

        }
    }

    enum class Severity {
        Info,
        Error,
        Warning,
        Debug
    }

    data class Line(
        val severity: Severity,
        val time: Info.TimePoint,
        val onCreateTimeThreadInfo: Info.Thread,
        val investObj: Info.Instance,
        val msg: String
    )

    val createTimeThread = Info.Thread.current()

    fun info(any: Any?) {
//put()
    }

    companion object {

        fun forInstance(any: Any) = Info.Instance.of(any)

        fun put(createTimeThread: Info.Thread, investObj: Info.Instance, msg: String) {

        }
    }
}