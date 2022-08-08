@Suppress("unused")
object Action02 : ScriptStr {
    private const val id = "QwsDebugAction02"
    private const val keymapShortcut = "ctrl alt shift F10"

    override fun script(runtimeMap: Map<String, Any?>): String {
        Lib.action.register(id, keymapShortcut, IdeDumbAwareAction.create { actionEvent ->
            val project = actionEvent.project ?: return@create
            if (project.name != Const.qwsProjectName) return@create
            val outputPanel = Lib.outputPanel(project.name, Const.consolePanelName) ?: return@create

            ApplicationMgr.getApplication().runWriteAction { FileDocumentMgr.getInstance().saveAllDocuments() }

            val fileEditorManager = FileEditorMgrEx.getInstance(project) as FileEditorMgrEx
            val currentFile = fileEditorManager.currentFile

            outputPanel.out println "from $id  ${Const.debugLabel} $currentFile"
            outputPanel.toFrontRunContent()
        })

        return "[${Const.debugLabel}] action $id registered"
    }
}