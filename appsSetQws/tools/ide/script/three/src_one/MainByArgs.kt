object MainByArgs : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println(" ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        val IDE: IDE_typealias by runtimeMap.bindings
        IDE.error("...debug")

        scriptStrResult append "bindings=${runtimeMap.bindings}\n"
        scriptStrResult append "${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(
            chanelId = chanelIdIdeScriptListener,
            uniqueString = "forIde",
            needBindings = true,
            runEnv = runEnv.copy(needTmpDirQuick = true),
        )
    }
}