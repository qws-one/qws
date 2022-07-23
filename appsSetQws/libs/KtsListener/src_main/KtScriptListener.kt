abstract class KtScriptListener(val chanelId: Int) {

    abstract fun newScriptEngine(): KtScriptEngine?

    fun start(outputPanel: OutputPanel) {
        Thread {
            var ktsEngine = newScriptEngine()
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
                            //outputPanel println str
                            it.eval(str)?.run { toString() }
                                ?: "Script Result is Null"
                        } ?: "Script Engine is Null"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        outputPanel.err println "Run Script Error: $e"
                        val tmpFile = Fs.createTempFile("KtScriptListener", ".txt")
                        tmpFile.writeText(msg)
                        outputPanel.err println "file:///${tmpFile.absoluteFile}"
                        ktsEngine = null
                        "Script Result is Error: $e"
                    }
                    outputPanel println "chanel $chanelId"
                    //outputPanel println "chanel $chanelId scriptResult=${scriptResult}"
                    result(scriptResult)
                }
                outputPanel println "end $connectionIndex ${Thread.currentThread()} t=$t"
                if (null == ktsEngine) ktsEngine = newScriptEngine()
            }
            outputPanel println "listen $chanelId Thread   end ${Thread.currentThread()}"
        }.start()
    }
}