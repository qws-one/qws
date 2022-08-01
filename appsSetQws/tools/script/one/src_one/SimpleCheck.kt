object SimpleCheck {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConfLite {
            conf(
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }
        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, ToolSharedConfig.chanelId_ScriptListener)

        assert(socket.send("1+2") == "3")
//        val res = socket.send("q")
        val res = socket.send("bindings")
//        val res = socket.send("close")
//        val res = socket.send("this")
//        val res = socket.send("read")
        println("run result  '$res'")
    }
}