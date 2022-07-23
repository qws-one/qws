//

object KtsListener : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        val chanelId = ToolSharedConfig.chanelIdPlainTypeAliasIdeListener
        val project = ProjectMgr.getInstance().openProjects.first()
        ApplicationMgr.getApplication().invokeLater {
            val outputPanel = Lib.outputPanel(project.name, "KtsListener") ?: TODO()
            Thread {
                var ktsEngine = Lib.newScriptEngine()
                outputPanel println "listen $chanelId Thread start ${Thread.currentThread()}"
                LocalHostSocket.uds(chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
                    outputPanel println "beg chanel=$chanelId index=$connectionIndex ${Thread.currentThread()} "
                    val t = kotlin.system.measureTimeMillis {
                        val scriptResult = try {
                            val commandClose = "close"
                            val commandRead = "read"
                            val str = when {
                                msg == commandClose -> "".also { closeChannel() }
                                msg.startsWith(commandRead) -> msg.substring(commandRead.length).trim().let { Fs._file(it).readText() }
                                else -> msg
                            }
                            ktsEngine?.let {
                                outputPanel println str
                                it.eval(str)?.run { toString() }
                                    ?: "Script Result is Null"
                            } ?: "Script Engine is Null"
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ktsEngine = null
                            "Script Result is Error: $e"
                        }
                        outputPanel println "chanel $chanelId scriptResult=${scriptResult}"
                        result(scriptResult)
                    }
                    outputPanel println "end $connectionIndex ${Thread.currentThread()} t=$t"
                    if (null == ktsEngine) ktsEngine = Lib.newScriptEngine()
                }
                outputPanel println "listen $chanelId Thread   end ${Thread.currentThread()}"
            }.start()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(dummyChanelId, saveToPlace = placeInModuleAndIdeScripting("$ide_scripting/$ide_scripting-KtsListener.kts"))
    }
}