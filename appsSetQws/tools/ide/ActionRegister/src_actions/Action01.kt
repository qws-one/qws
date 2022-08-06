@Suppress("unused")
object Action01 : ScriptStr {
    private const val id = "QwsDebugAction01"
    private const val keymapShortcut = "ctrl alt shift F9"

    override fun script(runtimeMap: Map<String, Any?>): String {
        Lib.action.register(id, keymapShortcut, IdeDumbAwareAction.create { actionEvent ->
            val project = actionEvent.project ?: return@create
            if (project.name != Const.qwsProjectName) return@create
            val outputPanel = Lib.outputPanel(project.name, Const.consolePanelName) ?: return@create
            outputPanel.toFrontRunContent()
            outputPanel.out println "from $id  ${Const.debugLabel}"
        })
        return "[${Const.debugLabel}] action $id registered"
    }
}