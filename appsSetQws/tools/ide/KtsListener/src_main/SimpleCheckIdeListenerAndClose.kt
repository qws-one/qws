object SimpleCheckIdeListenerAndClose {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConf(ModuleInfo) {
            conf(
                chanelId = chanelIdIdeScriptListener,
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }

        LocalHostSocket.configureToSystemOut()
        //LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, 8712).send(ExecScriptListener.commandClose)
        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, conf.chanelId)

        println("run result  '${socket.send("this")}'")
        println("run result  '${socket.send(ExecScriptListener.commandClose)}'")
    }
}