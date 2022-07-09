//

@Suppress("unused", "UNUSED_PARAMETER")
object ReloadActions {
    private const val id = "QwsDebugReloadActions"
    private const val keymapShortcut = "ctrl alt shift F12"

    private fun go(filePath: String, outputPanel: Lib.OutputPanel) {
        Lib.newScriptEngine()?.run {
            val scriptFile = java.io.File(filePath)
            val moduleDir = scriptFile.parentFile.parentFile
            val typealiasFile/**/ = java.io.File(moduleDir, "lib/TypeAlias.kt")
            val libFile /*     */ = java.io.File(moduleDir, "lib/Lib.kt")
            val importsFile /* */ = java.io.File(moduleDir, "tmp/imports.txt")

            val scriptStr = scriptFile.readText()
            val scriptSha1 = Lib.sha1sum(scriptStr)

            val script = """
${importsFile.readText()}
${typealiasFile.readText().replace("import typealias4ide.*", "")}
${libFile.readText()}
$scriptStr

val res = ${scriptFile.nameWithoutExtension}.script(arrayOf("${scriptFile.absolutePath}", "$scriptSha1"), bindings)
res
    """
//            java.io.File(moduleDir, "tmp/reload-${scriptFile.name}.kts")
//                .apply { parentFile.mkdirs(); writeText(script); outputPanel.out("save to :$absolutePath") }
            try {
                val res = eval(script)
                if (null != res && Unit != res) outputPanel.out(res)
            } catch (e: Exception) {
                e.printStackTrace()
                outputPanel.err(e)
            }
        }
    }

    private fun reloadActions(scriptSha1: String, project: IdeProject, outputPanel: Lib.OutputPanel) {
        val basePath = project.basePath ?: return

        ApplicationMgr.getApplication().runWriteAction { FileDocumentMgr.getInstance().saveAllDocuments() }

        outputPanel.out println "from $id  ${Lib.debugLabel} start reload actions"
        val placeOfModule = "tools/ide/ActionRegister"
        java.io.File("$basePath/$placeOfModule/src_actions/").listFiles()?.forEach {
            go(it.absolutePath, outputPanel)
        }
        val scriptFile = java.io.File("$basePath/$placeOfModule/tool/ReloadActions.kt")
        val scriptFileSha1 = Lib.sha1sum(scriptFile.readText())

        outputPanel.out println "from $id  ${Lib.debugLabel} scriptFileSha1=$scriptFileSha1 scriptSha1=$scriptSha1"
        if (scriptSha1 != scriptFileSha1) go(scriptFile.absolutePath, outputPanel)
        outputPanel.toFrontRunContent()
    }

    fun script(args: Array<String>, bindings: Map<String, Any?>): Any {
        val scriptSha1 = args[1]
        Lib.registerAction(id, keymapShortcut, IdeDumbAwareAction.create { actionEvent ->
            val project = actionEvent.project ?: return@create
            if (project.name != Lib.qwsProjectName) return@create
            val outputPanel = Lib.outputPanel(project.name, Lib.consolePanelName) ?: return@create
            outputPanel.toFrontRunContent()
            ApplicationMgr.getApplication().invokeLater {
                reloadActions(scriptSha1, project, outputPanel)
            }
        })
        return "[${Lib.debugLabel}] action $id registered"
    }
}
