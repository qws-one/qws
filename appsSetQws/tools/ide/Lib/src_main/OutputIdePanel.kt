class OutputIdePanel(val projectName: String, val descriptor: IdeRunContentDescriptor, val descriptorDisplayName: String, val type: Type) : OutputPanel {
    enum class Type(val executor: () -> IdeExecutor) {
        Run({ IdeDefaultRunExecutor.getRunExecutorInstance() }),
        Dbg({ IdeDefaultDebugExecutor.getDebugExecutorInstance() })
    }

    private fun Any?.string() = (this?.toString() ?: "null")

    @Suppress("SpellCheckingInspection")
    private fun Any?.stringln() = this.string() + "\n"

    private val consoleView get() = descriptor.executionConsole as? IdeConsoleView

    override infix fun println(msg: Any?) {
        consoleView?.print(msg.stringln(), IdeConsoleViewContentType.NORMAL_OUTPUT)
    }

    override val out = object : OutputPanel.Out {
        override fun print(msg: Any?) {
            consoleView?.print(msg.string(), IdeConsoleViewContentType.NORMAL_OUTPUT)
        }

        override fun println(msg: Any?) {
            consoleView?.print(msg.stringln(), IdeConsoleViewContentType.NORMAL_OUTPUT)
        }
    }

    override val err = object : OutputPanel.Out {
        override fun print(msg: Any?) {
            consoleView?.print(msg.string(), IdeConsoleViewContentType.ERROR_OUTPUT)
        }

        override fun println(msg: Any?) {
            consoleView?.print(msg.stringln(), IdeConsoleViewContentType.ERROR_OUTPUT)
        }
    }

    fun printHyperlink(msg: Any?, block: (IdeProject) -> Unit) =
        consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block(it) })

    override fun printHyperlinkDummy(msg: Any?, block: () -> Unit) {
        consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block() })
    }

    @Suppress("DEPRECATION")
    override fun toFrontRunContent(): OutputIdePanel {
        ProjectMgr.getInstance().openProjects.forEach {
            if (projectName == it.name) {
                ApplicationMgr.getApplication().invokeLater {
                    ExecutionMgr.getInstance(it).getContentManager().toFrontRunContent(type.executor(), descriptor)
                }
            }
        }
        return this
    }
}
