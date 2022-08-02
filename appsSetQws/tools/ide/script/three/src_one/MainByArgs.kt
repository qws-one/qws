object MainByArgs : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        scriptStrResult put "${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            uniqueString = "forIde",
            runEnv = runEnv.copy(needTmpDirQuick = true),
        )
    }
}