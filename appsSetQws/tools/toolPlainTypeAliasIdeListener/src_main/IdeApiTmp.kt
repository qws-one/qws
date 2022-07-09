typealias BorderLayout = java.awt.BorderLayout
typealias JPanel = javax.swing.JPanel
typealias KeyStroke = javax.swing.KeyStroke

object IdeApiTmp {

    class ScriptEngine(val engine: IdeScriptEngine_typealias?) {

        fun eval(str: String): Any? {
            return engine?.eval(str)
        }
    }

    fun scriptEngine(): ScriptEngine? {
        for (engineInfo in IdeScriptEngineMgr.getInstance().engineInfos) {
            if (engineInfo.fileExtensions.contains("kts")) {
                return ScriptEngine(IdeScriptEngineMgr.getInstance().getEngine(engineInfo, null as ClassLoader?))
            }
        }
        return null
    }

    class ConsolePanel(val projectName: String, val descriptor: IdeRunContentDescriptor, val descriptorDisplayName: String, val type: Type) {
        enum class Type(val executor: () -> IdeExecutor) {
            Run({ IdeDefaultRunExecutor.getRunExecutorInstance() }),
            Dbg({ IdeDefaultDebugExecutor.getDebugExecutorInstance() })
        }

        private val consoleView get() = descriptor.executionConsole as? IdeConsoleView

        private fun Any?.string() = (this?.toString() ?: "null") + "\n"

        fun print(msg: Any?) = consoleView?.print(msg.string(), IdeConsoleViewContentType.NORMAL_OUTPUT)

        fun error(msg: Any?) = consoleView?.print(msg.string(), IdeConsoleViewContentType.ERROR_OUTPUT)

        fun printHyperlink(msg: Any?, block: (IdeProject) -> Unit) =
            consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block(it) })

        fun printHyperlinkDummy(msg: Any?, block: () -> Unit) = consoleView?.printHyperlink(msg.string(), IdeHyperlinkInfo { block() })

        fun toFrontRunContent(): ConsolePanel {
            ProjectMgr.getInstance().openProjects.forEach {
                if (projectName == it.name) {
                    ApplicationMgr.getApplication().invokeLater {
                        val executor = type.executor()
//                        RunContentManager.getInstance(it).toFrontRunContent(executor, descriptor))
                        ExecutionMgr.getInstance(it).getContentManager().toFrontRunContent(executor, descriptor)
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

    fun consolePanel(projectName: String, displayName: String, type: ConsolePanel.Type = ConsolePanel.Type.Run): ConsolePanel? {
        val project = fromOpenProjects(projectName) ?: return null
        val descriptorDisplayName = type.name + displayName.replaceFirstChar { it.uppercaseChar() }
        for (runContentDescriptor in ExecutionMgr.getInstance(project).getContentManager().allDescriptors) {
            if (null != runContentDescriptor && runContentDescriptor.displayName == descriptorDisplayName) {
                return ConsolePanel(project.name, runContentDescriptor, descriptorDisplayName, type)
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
            ConsolePanel(project.name, descriptor, descriptorDisplayName, type)
        }
    }

    fun registerAction(id: String, block: () -> Unit) = registerAction(id, IdeDumbAwareAction.create { actionEvent ->
        actionEvent?.let { block() }
    })

    fun registerAction(id: String, action: IdeAnAction) = ActionMgr.getInstance().apply {
        if (getAction(id) != null) {
            unregisterAction(id)
        }
        registerAction(id, action)
        action.templatePresentation.setText("QWS DEBUG $id", true)
    }
}

object Tmp001 {
    fun script(args: Array<String>, bindings: Map<String, Any?>) {
        ProjectMgr.getInstance().openProjects.forEach { project ->
            if ("qws" == project.name) IdeApiTmp.consolePanel(project.name, "QWS check")?.let { consolePanel ->
                consolePanel.toFrontRunContent()

                consolePanel.print("from script")
                consolePanel.printHyperlink("Hyperlink") { consolePanel.print("from Hyperlink project=$it") }
                consolePanel.printHyperlinkDummy("HyperlinkDummy") { consolePanel.print("from HyperlinkDummy") }

                IdeApiTmp.registerAction("qws_one") {
                    consolePanel.toFrontRunContent().print("from action qws_one " + IdeApiTmp.scriptEngine()?.eval("1+12"))
                }
            }
        }

//        IdeLocalFileSystem.getInstance().findFileByIoFile(File(project.basePath, "ide/plugin01/src_kt_ide/local_tool/A.kt"))
//            ?.let { virtualFile ->
//                FileEditorMgr.getInstance(project).openFile(virtualFile, true)
//                ApplicationMgr.getApplication().invokeLater {
//                }
//            }
    }
}

//Tmp001.script(emptyArray(), bindings)