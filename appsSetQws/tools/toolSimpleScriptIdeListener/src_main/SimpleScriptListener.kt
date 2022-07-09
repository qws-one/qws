//
@Suppress("unused")
object SimpleScriptListener {

    val chanelId = ToolSharedConfig.chanelIdSimpleScriptListener

    fun start(bindings: Map<String, Any?>) {
        val ideOutPrintln: (o: Any) -> Unit by bindings
        val ideErrPrintln: (o: Any) -> Unit by bindings
        ideOutPrintln("SimpleScriptListener.main")

        val additionalScriptEngine: javax.script.ScriptEngine by bindings
        Thread {
            ideOutPrintln("Listener: listen $chanelId Thread start ${Thread.currentThread()}")
            LocalHostSocket.uds(chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
                ideOutPrintln("Listener: beg chanel=$chanelId index=$connectionIndex ${Thread.currentThread()} ")
                val t = kotlin.system.measureTimeMillis {
                    val scriptResult = try {
                        additionalScriptEngine.eval(msg)?.run { toString() } ?: "Script Result is Null"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ideErrPrintln("Listener: error=$e")
                        "Script Result is Error: $e"
                    }
                    ideOutPrintln("Listener: chanel $chanelId scriptResult=${scriptResult}")
                    result(scriptResult)
                }
                ideOutPrintln("Listener: end $connectionIndex ${Thread.currentThread()} t=$t")
            }
            ideOutPrintln("Listener: listen $chanelId Thread   end ${Thread.currentThread()}")
        }.start()
    }
}
