//

object KtsListener {
    @JvmStatic
    fun main(args: Array<String>) {
        val chanelId = ToolSharedConfig.chanelId_ScriptListener
        val scriptListener = object : KtScriptListener(chanelId) {
            override fun newScriptEngine() = javax.script.ScriptEngineManager().getEngineByExtension("kts")?.let {
                object : KtScriptEngine {
                    override fun eval(any: String) = it.eval(any)
                }
            }
        }
        scriptListener.start(OutputPanelSystemOut)
    }
}