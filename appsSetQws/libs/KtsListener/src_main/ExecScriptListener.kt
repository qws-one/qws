//abstract class ExecScriptListener(val chanelId: Int, val tmpDir: String = "") {
abstract class ExecScriptListener(private val localHostSocket: LocalHostSocket.SocketConfig) {

    abstract fun newScriptEngine(): ExecEngine?

    fun start(outputPanel: OutputPanel) {
        Thread {
            var ktsEngine = newScriptEngine()
            val socketId = localHostSocket.description()
            outputPanel println "listen '$socketId' Thread start ${Thread.currentThread()}"
            localHostSocket.listen {
                outputPanel println "beg chanel='$socketId' index=$connectionIndex ${Thread.currentThread()} "
                val t = kotlin.system.measureTimeMillis {
                    val commandClose = "close"
                    val commandRead = "read"
                    val commandReturnStrForExec = "//returnStrForExec"
                    var returnStrForExec = false
                    val str = when {
                        msg == commandClose -> "".also { closeChannel() }
                        msg.startsWith(commandRead) -> with(LocalFs) { msg.substring(commandRead.length).fsPath.file.readText() }
                        msg.startsWith(commandReturnStrForExec) -> msg.also { returnStrForExec = true }
                        else -> msg
                    }
                    val scriptResult = try {
                        ktsEngine?.let {
                            //outputPanel println str
                            it.exec(str)?.run { toString() }
                                ?: "Script Result is Null"
                        } ?: "Script Engine is Null"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        outputPanel.err println "Run Script Error: $e"
                        //val tmpFile = LocalFs.createTempFile("ExecScriptEngine", ".txt")
                        //tmpFile.writeText(msg)
                        //outputPanel.err println "file:///${tmpFile.absoluteFile}"
                        ktsEngine = null
                        "Script Result is Error: $e".let { if (returnStrForExec) "//---\n$str//===\n$it" else it }
                    }
                    outputPanel println "chanel '$socketId'"
                    //outputPanel println "chanel $chanelId scriptResult=${scriptResult}"
                    result(scriptResult)
                }
                outputPanel println "end $connectionIndex ${Thread.currentThread()} t=$t"
                if (null == ktsEngine) ktsEngine = newScriptEngine()
            }
            outputPanel println "listen '$socketId' Thread   end ${Thread.currentThread()}"
        }.start()
    }
}