object M2 : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        scriptStrResult put "${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(
            ToolSharedConfig.chanelId_IdeScriptListener,
            runEnv = runEnv.copy(needTmpDirQuick = true),
            saveToPlace = places.inIdeScripting("$ide_scripting/$ide_scripting-M2.kts"),
        )
    }
}