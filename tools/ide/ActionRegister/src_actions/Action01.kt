//

@Suppress("unused", "UNUSED_PARAMETER")
object Action01 {
    private const val id = "QwsDebugAction01"
    private const val keymapShortcut = "ctrl alt shift F9"

    fun script(args: Array<String>, bindings: Map<String, Any?>): Any {
        Lib.registerAction(id, keymapShortcut, IdeDumbAwareAction.create { actionEvent ->
            val project = actionEvent.project ?: return@create
            if (project.name != Lib.qwsProjectName) return@create
            val outputPanel = Lib.outputPanel(project.name, Lib.consolePanelName) ?: return@create
            outputPanel.toFrontRunContent()
            outputPanel.out println "from $id  ${Lib.debugLabel}"
        })
        return "[${Lib.debugLabel}] action $id registered"
    }
}
