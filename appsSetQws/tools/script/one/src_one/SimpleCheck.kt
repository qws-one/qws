object SimpleCheck {
    @JvmStatic
    fun main(args: Array<String>) {
        val chanelId = ToolSharedConfig.chanelId_ScriptListener
//        val res = LocalHostSocket.uds(chanelId).send("1+2")
//        val res = LocalHostSocket.uds(chanelId).send("q")
//        val res = LocalHostSocket.uds(chanelId).send("bindings")
        val res = LocalHostSocket.uds(chanelId).send("close")
//        val res = LocalHostSocket.uds(chanelId).send("this")
//        val res = LocalHostSocket.uds(chanelId).send("read")
        println("run result  '$res'")
    }
}