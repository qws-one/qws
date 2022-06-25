//

typealias BorderLayout = java.awt.BorderLayout
typealias JPanel = javax.swing.JPanel
typealias KeyStroke = javax.swing.KeyStroke

@Suppress("MemberVisibilityCanBePrivate")
object Lib {
    const val debugLabel = "debugLabel10"
    const val qwsProjectName = "qws"
    const val consolePanelName = "QWS Actions"
    const val actionNamePrefix = "QWS DEBUG"

    fun registerAction(id: String, keyStroke: String, action: IdeAnAction) {
        registerAction(id, action)
        val shortcut = IdeKeyboardShortcut(KeyStroke.getKeyStroke(keyStroke), null)
        val keymap: IdeKeymap = KeymapMgr.getInstance().activeKeymap;
        keymap.removeAllActionShortcuts(id)
        keymap.addShortcut(id, shortcut)
    }

    fun registerAction(id: String, action: IdeAnAction) = ActionMgr.getInstance().apply {
        if (getAction(id) != null) {
            KeymapMgr.getInstance().activeKeymap.removeAllActionShortcuts(id)
            unregisterAction(id)
        }
        registerAction(id, action)
        action.templatePresentation.setText("$actionNamePrefix $id", true)
    }

    fun sha1sum(str: String): String? = java.security.MessageDigest.getInstance("SHA-1")?.run {
        reset()
        update(str.toByteArray())
        val sum = java.lang.String.format("%040x", java.math.BigInteger(1, digest()))
        reset()
        sum
    }

    fun newScriptEngine(): IdeScriptEngine_typealias? {
        for (engineInfo in IdeScriptEngineMgr.getInstance().engineInfos) {
            if (engineInfo.fileExtensions.contains("kts")) {
                return IdeScriptEngineMgr.getInstance().getEngine(engineInfo, null as ClassLoader?)
            }
        }
        return null
    }

    class OutputPanel(val projectName: String, val descriptor: IdeRunContentDescriptor, val descriptorDisplayName: String, val type: Type) {
        enum class Type(val executor: () -> IdeExecutor) {
            Run({ IdeDefaultRunExecutor.getRunExecutorInstance() }),
            Dbg({ IdeDefaultDebugExecutor.getDebugExecutorInstance() })
        }

        interface Out {
            operator fun invoke(msg: Any?) = this.println(msg)
            infix fun print(msg: Any?)
            infix fun println(msg: Any?)
        }

        private fun Any?.string() = (this?.toString() ?: "null")

        @Suppress("SpellCheckingInspection")
        private fun Any?.stringln() = this.string() + "\n"

        private val consoleView get() = descriptor.executionConsole as? IdeConsoleView

        val out = object : Out {
            override fun print(msg: Any?) {
                consoleView?.print(msg.string(), IdeConsoleViewContentType.NORMAL_OUTPUT)
            }

            override fun println(msg: Any?) {
                consoleView?.print(msg.stringln(), IdeConsoleViewContentType.NORMAL_OUTPUT)
            }
        }

        val err = object : Out {
            override fun print(msg: Any?) {
                consoleView?.print(msg.string(), IdeConsoleViewContentType.ERROR_OUTPUT)
            }

            override fun println(msg: Any?) {
                consoleView?.print(msg.stringln(), IdeConsoleViewContentType.ERROR_OUTPUT)
            }
        }

        fun printHyperlink(msg: Any?, block: (IdeProject) -> Unit) =
            consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block(it) })

        fun printHyperlinkDummy(msg: Any?, block: () -> Unit) = consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block() })

        fun toFrontRunContent(): OutputPanel {
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

    fun fromOpenProjects(projectName: String): IdeProject? {
        ProjectMgr.getInstance().openProjects.forEach {
            if (projectName == it.name) return it
        }
        return null
    }

    fun outputPanel(projectName: String, displayName: String, type: OutputPanel.Type = OutputPanel.Type.Run): OutputPanel? {
        val project = fromOpenProjects(projectName) ?: return null
        val descriptorDisplayName = type.name + displayName.replaceFirstChar { it.uppercaseChar() }
        for (runContentDescriptor in ExecutionMgr.getInstance(project).getContentManager().allDescriptors) {
            if (null != runContentDescriptor && runContentDescriptor.displayName == descriptorDisplayName) {
                return OutputPanel(project.name, runContentDescriptor, descriptorDisplayName, type)
            }
        }
        return IdeDefaultActionGroup().let { group ->
            val consoleView = IdeTextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val panel = JPanel(BorderLayout())
            panel.add(consoleView.component, "Center")
            val toolbar = ActionMgr.getInstance().createActionToolbar("RunConsole", group, false)
            toolbar.targetComponent = consoleView.component
            panel.add(toolbar.component, "West")
            val descriptor = object : IdeRunContentDescriptor(consoleView, null as IdeProcessHandler?, panel, descriptorDisplayName) {
                override fun isContentReuseProhibited() = true
            }
            val executor = type.executor()
            group.addAll(*consoleView.createConsoleActions())
            group.add(IdeExecutionCloseAction(executor, descriptor, project))
            ExecutionMgr.getInstance(project).getContentManager().showRunContent(executor, descriptor)
            OutputPanel(project.name, descriptor, descriptorDisplayName, type)
        }
    }
}
