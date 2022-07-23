//

object KtsListener : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        val chanelId = ToolSharedConfig.chanelId_IdeScriptListener
        val project = ProjectMgr.getInstance().openProjects.first()
        ApplicationMgr.getApplication().invokeLater {
            val outputPanel = Lib.outputPanel(project.name, "KtsListener") ?: TODO()
            val scriptListener = object : KtScriptListener(chanelId) {
                override fun newScriptEngine() = Lib.newScriptEngine()?.let {
                    object : KtScriptEngine {
                        override fun eval(any: String) = it.eval(any)
                    }
                }
            }
            scriptListener.start(outputPanel)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(dummyChanelId, saveToPlace = placeInModuleAndIdeScripting("$ide_scripting/$ide_scripting-KtsListener.kts"))
    }
}