object MainByArgs : ScriptStr {
    private val className = this::class.simpleName

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("... $className")

        scriptStrResult put "$className ..."
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            uniqueString = "four",
            needBindings = true,
            needArgs = true,
            runEnv = runEnv.copy(needTmpDirQuick = true),
        )
    }
}