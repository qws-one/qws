//

@Suppress("UNUSED_PARAMETER")
object Ide {

    class Application {
        fun invokeLater(runnable: Runnable) {}
        fun invokeLaterOnWriteThread(runnable: Runnable) {}
    }

    class Project {
        val name: String = TODO()
        val basePath: String? = null
    }

    val application: Application = TODO()

    val project: Project? = null

    fun print(any: Any?) {}

    fun error(any: Any?) {}

    class ScriptEngine() {
        lateinit var engine: ScriptEngine
        fun eval(str: String): Any? {
            return engine?.eval(str)
        }
    }

    fun scriptEngine(): ScriptEngine? = TODO()

    class ConsolePanel(val projectName: String) {

        enum class Type {
            Run,
            Dbg
        }


        private fun Any?.string() = (this?.toString() ?: "null") + "\n"

        fun print(msg: Any?): Unit = TODO()

        fun error(msg: Any?): Unit = TODO()

        fun printHyperlinkDummy(msg: Any?, block: () -> Unit): Unit = TODO()

        fun toFrontRunContent(): ConsolePanel {

            return this
        }
    }

    fun fromOpenProjects(projectName: String): Project? = TODO()

    fun consolePanel(projectName: String, displayName: String, type: ConsolePanel.Type = ConsolePanel.Type.Run): ConsolePanel = TODO()
    fun registerAction(id: String, block: () -> Unit): Unit = TODO()

}
