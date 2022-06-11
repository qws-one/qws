package qws_local
//
//
//

typealias ApplicationInstance = Any
typealias ProjectInstance = Any

class QwsLocal {

    @Suppress("ClassName")
    object jvmSystemOutLogger : Logger {
        override infix fun out(any: Any?) {
            println(any)
        }

        override infix fun err(any: Any?) {
            System.err.println(any)
        }

        override fun err(msg: String, throwable: Throwable) {
            System.err.println("Err: $msg\n$throwable\n${throwable.stackTrace.joinToString("\n")}")
        }
    }

    interface Logger {
        infix fun out(any: Any?)
        infix fun err(any: Any?)
        fun err(msg: String, throwable: Throwable)
    }

    open class Application(
            val name: String,
            val instance: ApplicationInstance
    ) {
        override fun toString(): String {
            return QwsUtils16.run { "name=$name appId=${instance.hashHex}" }
        }
    }

    open class Project(
            val name: String,
            val path: String,
            val instance: ProjectInstance
    ) {
        override fun toString(): String {
            return Qws.utils.run { "name=$name appId=${instance.hashHex} path=$path" }
        }
    }
}
