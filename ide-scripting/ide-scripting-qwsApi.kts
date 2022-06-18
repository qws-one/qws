@Suppress("ClassName", "unused")
object qws : Qws(
    utils.run {
        val instance = ApplicationInstance()
        app("name1_${instance.hashHex}", instance)
    },
    utils.run {
        val instance = ProjectInstance()
        prj("name1_${instance.hashHex}", "tmp", instance)
    },
    object : QwsLocal.Logger {
        override infix fun out(any: Any?) {
            Ide.print(any)
        }

        override infix fun err(any: Any?) {
            Ide.error(any)
        }

        override fun err(msg: String, throwable: Throwable) {
            Ide.error("Err: $msg\n$throwable\n${throwable.stackTrace.joinToString("\n")}")
        }
    })
