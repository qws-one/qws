//
object IdeAction : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        ProjectMgr.getInstance().openProjects.forEach { project ->
            if (BuildDescConst.IdeProjectName == project.name) Lib.outputPanel(project.name, "QWS check")?.let { consolePanel ->
                consolePanel.toFrontRunContent()

                consolePanel.out println ("from script")
                consolePanel.printHyperlink("Hyperlink") { consolePanel.out println ("from Hyperlink project=$it") }
                consolePanel.printHyperlinkDummy("HyperlinkDummy") { consolePanel.out println ("from HyperlinkDummy") }

                val actionId = "qwsOneDebug"
                Lib.action.register(actionId) {
                    consolePanel.toFrontRunContent().out println ("from action $actionId " + Lib.newScriptEngine()?.eval("1+14"))
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(dummyChanelId, saveToPlace = places.inModule("$ide_scripting/$ide_scripting-IdeAction.kts"))
    }
}