//

object KtsListener {
    @JvmStatic
    fun main(args: Array<String>) {
        val chanelId = ToolSharedConfig.chanelId_ScriptListener
        val conf = RunScriptStr.buildConfLite {
            conf(
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }
        val scriptListener = object : ExecScriptListener(chanelId, conf.forRuntime.tmpDirQuick) {
            override fun newScriptEngine() = javax.script.ScriptEngineManager().getEngineByExtension("kts")?.let {
                object : ExecScriptEngine {
                    override fun exec(any: String) = it.eval(any)
                }
            }
        }
        scriptListener.start(OutputPanelSystemOut)
    }
}