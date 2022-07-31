abstract class ExecScriptListener(val chanelId: Int, val tmpDir: String = "") {

    abstract fun newScriptEngine(): ExecScriptEngine?

    fun start(outputPanel: OutputPanel) {
        Thread {
            var ktsEngine = newScriptEngine()
            outputPanel println "listen $chanelId Thread start ${Thread.currentThread()}"
            LocalHostSocket.uds(tmpDir, chanelId).params(acceptClientConnectionCount = Int.MAX_VALUE).listen {
                outputPanel println "beg chanel=$chanelId index=$connectionIndex ${Thread.currentThread()} "
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