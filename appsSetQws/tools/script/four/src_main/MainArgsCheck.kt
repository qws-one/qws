object MainArgsCheck : ScriptStr {
    private val className = this::class.simpleName

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("... $className")
        println("MainArgsCheck.script bindings   =})>${runtimeMap.bindings}<({")
        println("MainArgsCheck.script scriptPath =})>${runtimeMap.scriptPath}<({")
        println("MainArgsCheck.script args       =})>${runtimeMap.args}<({")
        println("MainArgsCheck.script tmpDirBig  =})>${runtimeMap.tmpDirBig}<({")
        println("MainArgsCheck.script tmpDirQuick=})>${runtimeMap.tmpDirQuick}<({")
        scriptStrResult put "$className ..."
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            needBindings = true,
            needScriptPath = true,
            args = args.plus("one"),
            runEnv = runEnv.copy(needTmpDirQuick = true),
        )
    }
}