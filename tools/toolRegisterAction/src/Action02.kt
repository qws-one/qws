//

@Suppress("unused", "UNUSED_PARAMETER")
object Action02 {
    private const val id = "QwsDebugAction02"
    private const val keymapShortcut = "ctrl alt shift F10"


    fun script(args: Array<String>, bindings: Map<String, Any?>): Any {
        Lib.registerAction(id, keymapShortcut, IdeDumbAwareAction.create { actionEvent ->
            val project = actionEvent.project ?: return@create
            if (project.name != Lib.qwsProjectName) return@create
            val outputPanel = Lib.outputPanel(project.name, Lib.consolePanelName) ?: return@create

            ApplicationMgr.getApplication().runWriteAction { FileDocumentMgr.getInstance().saveAllDocuments() }

            val fileEditorManager = FileEditorMgrEx.getInstance(project) as FileEditorMgrEx
            val currentFile = fileEditorManager.currentFile;

            outputPanel.out println "from $id  ${Lib.debugLabel} $currentFile"
            outputPanel.toFrontRunContent()
        })

        return "[${Lib.debugLabel}] action $id registered"
    }
}
