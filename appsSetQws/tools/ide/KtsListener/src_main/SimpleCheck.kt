object SimpleCheck {
    @JvmStatic
    fun main(args: Array<String>) {
        val chanelId = ToolSharedConfig.chanelId_IdeScriptListener
//        val res = LocalHostSocket.uds(chanelId).send("1+2")
//        val res = LocalHostSocket.uds(chanelId).send("q")
        val res = LocalHostSocket.uds(chanelId).send("bindings")
//        val res = LocalHostSocket.uds(chanelId).send("close")
//        val res = LocalHostSocket.uds(chanelId).send("this")
//        val res = LocalHostSocket.uds(chanelId).send("read")
        println("run result  '$res'")

//        println(2*2)
//        println(2*2*2*2 * 2*2*2*2)                      //  256
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2)            // 4096
//        println(2*2*2*2 * 2*2*2*2 * 2*2*2*2 * 2*2*2*2)  //65536
    }
}