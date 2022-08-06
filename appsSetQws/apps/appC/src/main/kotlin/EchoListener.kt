//

object EchoListener {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConfLite {
            conf(
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }

        LocalHostSocket.configureToSystemOut()
        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, conf.chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE)
        if (!socket.isFree) {
            //throw RuntimeException(" looks like already in use")
            socket.tryToFree()
        }
        val scriptListener = object : ExecScriptListener(socket) {
            override fun newExecEngine() = object : ExecEngine {
                override fun exec(any: String) = "echo: '$any'"
            }
        }
        scriptListener.start(OutputPanelSystemOut)
    }
}