object SimpleCheckIde {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = RunScriptStr.buildConf(ModuleInfo) {
            conf(
                chanelId = chanelIdIdeScriptListener,
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }

        LocalHostSocket.configureToSystemOut()
        val socket = LocalHostSocket.uds(conf.forRuntime.tmpDirQuick, conf.chanelId)

        assert(socket.send("1+2") == "3")
        println("run result  '${socket.send("bindings")}'")
        println("run result  '${socket.send("this")}'")
        println("run result  '${socket.send("this")}'")
        println("run result  '${socket.send("q")}'")
        println("run result  '${socket.send("this")}'")
        //println("run result  '${socket.send("read")}'")
//        println("run result  '${socket.send(ExecScriptListener.commandClose)}'")

//        println(2*2)
//        println(2*2*2*2 * 2*2*2*2)                      //  256
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2)            // 4096
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2 * 2*2*2*2)  //65536
    }
}