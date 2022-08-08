//

object IdeListener : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        //println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        val chanelId = ToolSharedConfig.chanelId_IdeScriptListener
        val project = ProjectMgr.getInstance().openProjects.first()
        val socket = LocalHostSocket.uds(runtimeMap.tmpDirQuick, chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE)
        scriptStrResult put "try to start on '${socket.description()}'"
        ApplicationMgr.getApplication().invokeLater {
            val outputPanel = Lib.outputPanel(project.name, "KtsListener") ?: TODO()
            //LocalHostSocket.configureLogDebugTo { outputPanel.out.println(it) }
            LocalHostSocket.configureLogInfoTo { outputPanel.out.println(it) }
            LocalHostSocket.configureLogErrTo { outputPanel.err.println(it) }
            if (!socket.isFree) "'${socket.description()}', looks like already in use.".let {
                outputPanel.err println it

                @Suppress("LocalVariableName")
                val IDE: IDE_typealias by runtimeMap.bindings
                IDE.error(it)
                throw RuntimeException(it)
                //socket.tryToFree().also { IDE.print("LocalHostSocket: result of tryToFree '${socket.description()}' is: $it") }
            }
            val scriptEngineBindings = "IDE".let { val ideObj = runtimeMap.bindings[it]; mapOf(it to ideObj) }
            val scriptListener = object : ExecScriptListener(socket) {
                override fun newExecEngine() = Lib.newScriptEngine(scriptEngineBindings)?.let {
                    object : ExecEngine {
                        override fun exec(any: String) = it.eval(any)
                    }
                }
            }
            scriptListener.start(outputPanel)
            outputPanel.toFrontRunContent()
            var whiteClick = true
            outputPanel.printlnHyperlinkDummy("To stop listener '${socket.description()}' just click on this message") {
                if (whiteClick) {
                    socket.send(ExecScriptListener.commandClose) // todo : need more clear solution
                    whiteClick = false
                } else outputPanel.usr println "Looks like listener already stopped :)"
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(
            dummyChanelId,
            needBindings = true,
            runEnv = runEnv.copy(needTmpDirQuick = true),
            saveToPlace = places.ktsInIdeScripting("KtsListener")
//            saveToPlace = places.ktsInModuleAndIdeScripting("KtsListener")
        )
    }
}