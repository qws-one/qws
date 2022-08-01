//

object IdeListener : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        val chanelId = ToolSharedConfig.chanelId_IdeScriptListener
        val project = ProjectMgr.getInstance().openProjects.first()
        ApplicationMgr.getApplication().invokeLater {
            val outputPanel = Lib.outputPanel(project.name, "KtsListener") ?: TODO()
            //LocalHostSocket.configureLogDebugTo { outputPanel.out.println(it) }
            LocalHostSocket.configureLogInfoTo { outputPanel.out.println(it) }
            LocalHostSocket.configureLogErrTo { outputPanel.err.println(it) }
            val socket = LocalHostSocket.uds(ScriptStrRunEnv.tmpDirQuick, chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE)
            if (!socket.isFree) " ${socket.description()}, looks like already in use.".let {
                outputPanel.err println it
                val bindings: Map<String, Any?> by runtimeMap

                @Suppress("LocalVariableName")
                val IDE: IDE_typealias by bindings
                IDE.error(it)
                throw RuntimeException(it)
                //socket.tryToFree().also { IDE.print("LocalHostSocket: result of tryToFree '${socket.description()}' is: $it") }
            }
            val scriptListener = object : ExecScriptListener(socket) {
                override fun newScriptEngine() = Lib.newScriptEngine()?.let {
                    object : ExecEngine {
                        override fun exec(any: String) = it.eval(any)
                    }
                }
            }
            scriptListener.start(outputPanel)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(
            dummyChanelId,
            runEnv = runEnv.copy(needTmpDirQuick = true),
            saveToPlace = places.inModuleAndIdeScripting("$ide_scripting/$ide_scripting-KtsListener.kts")
        )
    }
}