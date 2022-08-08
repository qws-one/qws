object M5 : ScriptStr {

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("${runtimeMap.scriptName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")

        @Suppress("LocalVariableName") val IDE by runtimeMap.bindings

        scriptStrResult put "$IDE  ${runtimeMap.bindings} \n ${ScriptStrRunEnv.scriptPath} ${this::class.simpleName} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(args) {
        conf(args = args).copy(
            chanelId = dummyChanelId,
            needBindings = true,
            saveToPlace = places.ktsInIdeScripting(objectName),
            debugCase_fromFile = true,
            withoutRunScriptStr = true,
        )
    }
}