object M4 : ScriptStr {
    private val scriptName = this::class.simpleName ?: this::class.java.simpleName

    override fun script(runtimeMap: Map<String, Any?>) = ScriptStr {
        println("$scriptName ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}")
        //val bindings: Map<String, Any?>?  try{ by runtimeMap} catch (e:Exception){}
        //val bindings = "bindings".let { runtimeMap.containsKey(it); runtimeMap[it] as? Map<*, *> }
        //val ide = bindings?.get("IDE")
        val bindings = runtimeMap.bindings
        val ide = bindings["IDE"]//
        scriptStrResult put "$ide $bindings \n ${ScriptStrRunEnv.scriptPath} ${ScriptStrRunEnv.appsPlacePath} ${ModuleInfo.appsSetName} ${ModuleInfo.name} ${ModuleInfo.relativePath} ${ModuleInfo.dependenciesSrcCsv}"
    }

    @JvmStatic
    fun main(args: Array<String>) = RunScriptStr(ModuleInfo) {
        conf.copy(
            chanelId = dummyChanelId,
            needBindings = true,
            //needSaveToFileDebug = true,
            saveToPlace = places.ktsInIdeScripting(scriptName),
            debugCase_fromFile = true,
            withoutRunScriptStr = true,
            runEnv = runEnv.copy(needAppsPlacePath = true)
        )
    }
}