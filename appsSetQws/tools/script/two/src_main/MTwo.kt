object MTwo : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        scriptStrResult put "${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf(
            dummyChanelId,
//            ToolSharedConfig.chanelId_IdeScriptListener,
            runEnv = runEnv.copy(needTmpDirQuick = true),
            withoutRunScriptStr = true,
            saveToPlace = places.inIdeScripting("$ide_scripting/$ide_scripting-MTwo.kts"),
        )
    }
}