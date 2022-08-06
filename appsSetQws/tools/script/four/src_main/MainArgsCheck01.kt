object MainArgsCheck01 : ScriptStr {
    private val className = this::class.simpleName

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("... $className")

        scriptStrResult put "$className ..."
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("MainArgsCheck01.main")
        RunScriptStr.invokeDebug(args) {
            conf(
                needBindings = true,
                needScriptPath = true,
                needArgs = true,
                runEnv = runEnv.copy(needTmpDirQuick = true),
            )
        }
    }
}