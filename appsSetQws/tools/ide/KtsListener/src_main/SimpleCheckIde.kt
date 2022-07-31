object SimpleCheckIde {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConf(ModuleInfo) {
            conf(
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }
        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, ToolSharedConfig.chanelId_IdeScriptListener)

//        val res = socket.send("1+2")
//        val res = socket.send("q")
        val res = socket.send("bindings")
//        val res = socket.send("close")
//        val res = socket.send("this")
//        val res = socket.send("read")
        println("run result  '$res'")

//        println(2*2)
//        println(2*2*2*2 * 2*2*2*2)                      //  256
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2)            // 4096
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2 * 2*2*2*2)  //65536
    }
}