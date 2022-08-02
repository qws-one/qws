//

object KtsListener {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConfLite {
            conf(
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }
        val outputPanel = OutputPanelSystemOut
        //LocalHostSocket.configureLogDebugTo { outputPanel.out.println(it) }
        LocalHostSocket.configureLogInfoTo { outputPanel.out.println(it) }
        LocalHostSocket.configureLogErrTo { outputPanel.err.println(it) }

        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, conf.chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE)
        if (!socket.isFree) {
            throw RuntimeException(" looks like already in use")
            //socket.tryToFree()
        }
        val scriptListener = object : ExecScriptListener(socket) {
            override fun newScriptEngine() = javax.script.ScriptEngineManager().getEngineByExtension("kts")?.let {
                object : ExecEngine {
                    override fun exec(any: String) = it.eval(any)
                }
            }
        }
        scriptListener.start(outputPanel)
    }
}