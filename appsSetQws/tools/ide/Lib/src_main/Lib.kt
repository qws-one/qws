@Suppress("MemberVisibilityCanBePrivate", "DEPRECATION")
object Lib {

    @Suppress("ClassName")
    object action {
        const val namePrefix = "IdeLib DEBUG"

        fun register(id: String, keyStroke: String, action: IdeAnAction) {
            register(id, action)
            val shortcut = IdeKeyboardShortcut(JvmKeyStroke.getKeyStroke(keyStroke), null)
            val keymap: IdeKeymap = KeymapMgr.getInstance().activeKeymap
            keymap.removeAllActionShortcuts(id)
            keymap.addShortcut(id, shortcut)
        }

        fun register(id: String, action: IdeAnAction) = ActionMgr.getInstance().apply {
            unregister(id)
            registerAction(id, action)
            action.templatePresentation.setText("$namePrefix $id", true)
        }

        fun register(id: String, block: () -> Unit) = register(id, IdeDumbAwareAction.create { block() })

        @Suppress("unused")
        fun unregister(id: String) = ActionMgr.getInstance().unregister(id)

        @Suppress("NOTHING_TO_INLINE")
        inline fun ActionMgr.unregister(id: String) {
            if (getAction(id) != null) {
                KeymapMgr.getInstance().activeKeymap.removeAllActionShortcuts(id)
                unregisterAction(id)
            }
        }
    }

    fun newScriptEngine(): IdeScriptEngine_typealias? {
        for (engineInfo in IdeScriptEngineMgr.getInstance().engineInfos) {
            if (engineInfo.fileExtensions.contains("kts")) {
                return IdeScriptEngineMgr.getInstance().getEngine(engineInfo, null as ClassLoader?)
            }
        }
        return null
    }

    fun fromOpenProjects(projectName: String): IdeProject? {
        ProjectMgr.getInstance().openProjects.forEach {
            if (projectName == it.name) return it
        }
        return null
    }

    fun outputPanel(projectName: String, displayName: String, type: OutputIdePanel.Type = OutputIdePanel.Type.Run): OutputIdePanel? {
        val project = fromOpenProjects(projectName) ?: return null
        val descriptorDisplayName = type.name + displayName.replaceFirstChar { it.uppercaseChar() }
        for (runContentDescriptor in ExecutionMgr.getInstance(project).getContentManager().allDescriptors) {
            if (null != runContentDescriptor && runContentDescriptor.displayName == descriptorDisplayName) {
                return OutputIdePanel(project.name, runContentDescriptor, descriptorDisplayName, type)
            }
        }
        return IdeDefaultActionGroup().let { group ->
            val consoleView = IdeTextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val panel = JvmJPanel(JvmBorderLayout())
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
            OutputIdePanel(project.name, descriptor, descriptorDisplayName, type)
        }
    }
}
